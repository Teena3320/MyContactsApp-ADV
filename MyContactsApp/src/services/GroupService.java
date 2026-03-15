package services;

import domain.Contact;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GroupService {

    private final ContactRepository repository;
    private final Map<UUID, Map<String, ContactGroup>> byOwner = new ConcurrentHashMap<>();

    public GroupService(ContactRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public boolean createGroup(UUID ownerId, String name) {
        if (name == null || name.isBlank()) return false;
        byOwner.computeIfAbsent(ownerId, k -> new ConcurrentHashMap<>());
        Map<String, ContactGroup> groups = byOwner.get(ownerId);
        if (groups.containsKey(name)) return false;
        groups.put(name, new ContactGroup(name));
        return true;
    }

    public boolean addContact(UUID ownerId, String groupName, UUID contactId) {
        ContactGroup g = get(ownerId, groupName);
        if (g == null) return false;
        Optional<Contact> c = repository.findById(contactId);
        if (c.isEmpty() || !c.get().getOwnerUserId().equals(ownerId)) return false;
        // prevent duplicate
        if (g.flattenContactIds().contains(contactId)) return true;
        return g.addChild(new ContactLeaf(contactId));
    }

    public boolean removeContact(UUID ownerId, String groupName, UUID contactId) {
        ContactGroup g = get(ownerId, groupName);
        if (g == null) return false;
        return g.removeContact(contactId);
    }

    public List<String> listGroupNames(UUID ownerId) {
        Map<String, ContactGroup> groups = byOwner.getOrDefault(ownerId, Collections.emptyMap());
        List<String> names = new ArrayList<>(groups.keySet());
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    public Set<UUID> flattenContactIds(UUID ownerId, String groupName) {
        ContactGroup g = get(ownerId, groupName);
        if (g == null) return Set.of();
        return g.flattenContactIds();
    }

    private ContactGroup get(UUID ownerId, String groupName) {
        return byOwner.getOrDefault(ownerId, Collections.emptyMap()).get(groupName);
    }
}