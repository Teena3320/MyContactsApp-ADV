package services;

import domain.Contact;
import domain.Tag;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TagService {

    private final ContactRepository repository;

    private final Map<UUID, Map<String, Tag>> tagsByOwner = new HashMap<>();

    private final Map<UUID, Map<UUID, Set<Tag>>> contactTags = new HashMap<>();

    private final List<TagObserver> observers = new CopyOnWriteArrayList<>();

    public TagService(ContactRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void addObserver(TagObserver observer) {
        if (observer != null) observers.add(observer);
    }

    public void removeObserver(TagObserver observer) {
        observers.remove(observer);
    }

    private void notifyAssigned(UUID ownerId, UUID contactId, Tag tag) {
        TagEvent evt = new TagEvent(ownerId, contactId, tag, TagEventType.ASSIGNED, LocalDateTime.now());
        for (TagObserver o : observers) o.onTagChanged(evt);
    }

    private void notifyRemoved(UUID ownerId, UUID contactId, Tag tag) {
        TagEvent evt = new TagEvent(ownerId, contactId, tag, TagEventType.REMOVED, LocalDateTime.now());
        for (TagObserver o : observers) o.onTagChanged(evt);
    }

    public boolean createTag(UUID ownerId, String tagName) {
        String norm = normalize(tagName);
        if (norm == null) return false;
        Map<String, Tag> map = tagsByOwner.computeIfAbsent(ownerId, k -> new HashMap<>());
        if (map.containsKey(norm)) return false;
        map.put(norm, new Tag(tagName));
        return true;
    }

    public boolean deleteTag(UUID ownerId, String tagName) {
        String norm = normalize(tagName);
        if (norm == null) return false;

        Map<String, Tag> map = tagsByOwner.get(ownerId);
        if (map == null) return false;

        Tag removed = map.remove(norm);
        if (removed == null) return false;

        Map<UUID, Set<Tag>> assoc = contactTags.getOrDefault(ownerId, Collections.emptyMap());
        for (Map.Entry<UUID, Set<Tag>> e : assoc.entrySet()) {
            if (e.getValue().remove(removed)) {
                notifyRemoved(ownerId, e.getKey(), removed);
            }
        }
        return true;
    }

    public List<Tag> listTags(UUID ownerId) {
        Map<String, Tag> map = tagsByOwner.get(ownerId);
        if (map == null || map.isEmpty()) return List.of();
        List<Tag> out = new ArrayList<>(map.values());
        out.sort(Comparator.comparing(Tag::getDisplay, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    public boolean assignTag(UUID ownerId, UUID contactId, String tagName) {
        String norm = normalize(tagName);
        if (norm == null) return false;

        Optional<Contact> found = repository.findById(contactId);
        if (found.isEmpty() || !found.get().getOwnerUserId().equals(ownerId)) return false;

        Tag tag = tagsByOwner
                .computeIfAbsent(ownerId, k -> new HashMap<>())
                .computeIfAbsent(norm, k -> new Tag(tagName)); // create on first use

        Map<UUID, Set<Tag>> byContact = contactTags.computeIfAbsent(ownerId, k -> new HashMap<>());
        Set<Tag> set = byContact.computeIfAbsent(contactId, k -> new LinkedHashSet<>());
        boolean added = set.add(tag);
        if (added) notifyAssigned(ownerId, contactId, tag);
        return added;
    }

    public boolean removeTag(UUID ownerId, UUID contactId, String tagName) {
        String norm = normalize(tagName);
        if (norm == null) return false;

        Map<String, Tag> tagMap = tagsByOwner.getOrDefault(ownerId, Collections.emptyMap());
        Tag tag = tagMap.get(norm);
        if (tag == null) return false;

        Map<UUID, Set<Tag>> byContact = contactTags.getOrDefault(ownerId, Collections.emptyMap());
        Set<Tag> set = byContact.getOrDefault(contactId, Collections.emptySet());
        boolean removed = set.remove(tag);
        if (removed) notifyRemoved(ownerId, contactId, tag);
        return removed;
    }

    public Set<Tag> tagsForContact(UUID ownerId, UUID contactId) {
        Map<UUID, Set<Tag>> byContact = contactTags.get(ownerId);
        if (byContact == null) return Set.of();
        Set<Tag> set = byContact.get(contactId);
        if (set == null || set.isEmpty()) return Set.of();
        return Collections.unmodifiableSet(set);
    }

    public Set<UUID> contactsWithTag(UUID ownerId, String tagName) {
        String norm = normalize(tagName);
        if (norm == null) return Set.of();
        Map<String, Tag> map = tagsByOwner.getOrDefault(ownerId, Collections.emptyMap());
        Tag tag = map.get(norm);
        if (tag == null) return Set.of();

        Map<UUID, Set<Tag>> byContact = contactTags.getOrDefault(ownerId, Collections.emptyMap());
        Set<UUID> ids = new LinkedHashSet<>();
        for (Map.Entry<UUID, Set<Tag>> e : byContact.entrySet()) {
            if (e.getValue().contains(tag)) ids.add(e.getKey());
        }
        return ids;
    }

    private static String normalize(String name) {
        if (name == null) return null;
        String s = name.trim();
        if (s.isEmpty()) return null;
        return s.toLowerCase(Locale.ROOT);
    }
}