package services;

import domain.Contact;
import domain.Tag;

import java.util.*;

public class TagService {

    private final ContactRepository repository;

    // ownerId -> (normalizedTagName -> Tag)
    private final Map<UUID, Map<String, Tag>> tagsByOwner = new HashMap<>();

    // ownerId -> (contactId -> Set<Tag>)
    private final Map<UUID, Map<UUID, Set<Tag>>> contactTags = new HashMap<>();

    public TagService(ContactRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    // ---------- Tag CRUD (owner-scoped) ----------

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
        Map<String, Tag> map = tagsByOwner.getOrDefault(ownerId, Collections.emptyMap());
        Tag removed = (map instanceof HashMap<String, Tag> hm) ? hm.remove(norm) : null;
        if (removed == null) return false;

        // Remove from all contact associations
        Map<UUID, Set<Tag>> assoc = contactTags.getOrDefault(ownerId, Collections.emptyMap());
        for (Set<Tag> set : assoc.values()) {
            set.remove(removed);
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

    // ---------- Associations (Contact <-> Tag) ----------

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
        return set.add(tag);
    }

    public boolean removeTag(UUID ownerId, UUID contactId, String tagName) {
        String norm = normalize(tagName);
        if (norm == null) return false;

        Map<String, Tag> tagMap = tagsByOwner.getOrDefault(ownerId, Collections.emptyMap());
        Tag tag = tagMap.get(norm);
        if (tag == null) return false;

        Map<UUID, Set<Tag>> byContact = contactTags.getOrDefault(ownerId, Collections.emptyMap());
        Set<Tag> set = byContact.getOrDefault(contactId, Collections.emptySet());
        return set.remove(tag);
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

    // ---------- Helpers ----------

    private static String normalize(String name) {
        if (name == null) return null;
        String s = name.trim();
        if (s.isEmpty()) return null;
        return s.toLowerCase(Locale.ROOT);
    }
}