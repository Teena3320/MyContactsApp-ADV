package services;

import domain.Contact;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryContactRepository implements ContactRepository {

    private final Map<UUID, Contact> byId = new ConcurrentHashMap<>();
    private final Map<UUID, List<UUID>> byOwner = new ConcurrentHashMap<>();

    @Override
    public void save(Contact contact) {
        byId.put(contact.getId(), contact);
        byOwner.computeIfAbsent(contact.getOwnerUserId(), k -> new ArrayList<>());
        List<UUID> ids = byOwner.get(contact.getOwnerUserId());
        if (!ids.contains(contact.getId())) {
            ids.add(contact.getId());
        }
    }

    @Override
    public Optional<Contact> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Contact> findAllByOwner(UUID ownerUserId) {
        List<UUID> ids = byOwner.getOrDefault(ownerUserId, List.of());
        List<Contact> list = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            Contact c = byId.get(id);
            if (c != null) list.add(c);
        }
        return list;
    }
}