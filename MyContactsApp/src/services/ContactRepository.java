package services;

import domain.Contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository {
    void save(Contact contact);
    Optional<Contact> findById(UUID id);
    List<Contact> findAllByOwner(UUID ownerUserId);
}