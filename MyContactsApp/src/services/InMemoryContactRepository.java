package services;

import domain.Contact;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryContactRepository implements ContactRepository, SupportsHardDelete {

    private final Map<UUID, Contact> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Contact contact) {
        byId.put(contact.getId(), contact);
    }

    @Override
    public Optional<Contact> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Contact> findAllByOwner(UUID ownerUserId) {
        return byId.values().stream()
                .filter(c -> c.getOwnerUserId().equals(ownerUserId))
                .sorted(Comparator.comparing(Contact::getCreatedAt))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean deleteById(UUID id) {
        return byId.remove(id) != null;
    }
}