package services;

import domain.Contact;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContactDeletionService {

    private final ContactRepository repository;
    private final Map<UUID, Map<UUID, DeletedContact>> trash = new ConcurrentHashMap<>();

    public ContactDeletionService(ContactRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public boolean softDelete(UUID ownerId, UUID contactId) {
        Optional<Contact> found = repository.findById(contactId);
        if (found.isEmpty() || !found.get().getOwnerUserId().equals(ownerId)) return false;

        trash.computeIfAbsent(ownerId, k -> new ConcurrentHashMap<>())
                .put(contactId, new DeletedContact(found.get(), LocalDateTime.now()));

        if (repository instanceof SupportsHardDelete del) {
            del.deleteById(contactId);
            return true;
        }
        return false;
    }

    public boolean restore(UUID ownerId, UUID contactId) {
        Map<UUID, DeletedContact> userTrash = trash.getOrDefault(ownerId, Collections.emptyMap());
        DeletedContact dc = userTrash.get(contactId);
        if (dc == null) return false;

        repository.save(dc.contact());
        userTrash.remove(contactId);
        if (userTrash.isEmpty()) trash.remove(ownerId);
        return true;
    }

    public boolean purge(UUID ownerId, UUID contactId) {
        Map<UUID, DeletedContact> userTrash = trash.getOrDefault(ownerId, Collections.emptyMap());
        DeletedContact removed = userTrash.remove(contactId);
        if (userTrash.isEmpty()) trash.remove(ownerId);

        if (repository instanceof SupportsHardDelete del) {
            del.deleteById(contactId);
        }
        return removed != null;
    }

    public boolean hardDelete(UUID contactId) {
        if (repository instanceof SupportsHardDelete del) {
            boolean ok = del.deleteById(contactId);
            trash.values().forEach(map -> map.remove(contactId));
            trash.entrySet().removeIf(e -> e.getValue().isEmpty());
            return ok;
        }
        return false;
    }

    public List<DeletedContact> listTrash(UUID ownerId) {
        Map<UUID, DeletedContact> userTrash = trash.get(ownerId);
        if (userTrash == null || userTrash.isEmpty()) return List.of();
        return userTrash.values().stream()
                .sorted(Comparator.comparing(DeletedContact::deletedAt).reversed())
                .toList();
    }
}