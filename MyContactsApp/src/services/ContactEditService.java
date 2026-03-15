package services;

import domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ContactEditService {

    private final ContactRepository repository;

    public ContactEditService(ContactRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Optional<Contact> findById(UUID id) {
        return repository.findById(id);
    }

    public void save(Contact contact) {
        repository.save(contact);
    }

    public PersonContact rebuildPerson(PersonContact old,
                                       String newFirstNameOrNull,
                                       String newLastNameOrNull) {
        String first = newFirstNameOrNull != null ? newFirstNameOrNull : old.getFirstName();
        String last = newLastNameOrNull != null ? newLastNameOrNull : old.getLastName();
        String display = buildPersonDisplay(first, last, old.getDisplayName());

        return new PersonContact(
                old.getId(),
                old.getOwnerUserId(),
                display,
                old.getPhoneNumbers(),
                old.getEmailAddresses(),
                old.getCreatedAt(),
                LocalDateTime.now(),
                first,
                last
        );
    }

    public OrganizationContact rebuildOrganizationName(OrganizationContact old, String newOrgName) {
        String display = (newOrgName == null || newOrgName.isBlank())
                ? old.getDisplayName()
                : newOrgName.trim();

        return new OrganizationContact(
                old.getId(),
                old.getOwnerUserId(),
                display,
                old.getPhoneNumbers(),
                old.getEmailAddresses(),
                old.getCreatedAt(),
                LocalDateTime.now(),
                (newOrgName == null || newOrgName.isBlank()) ? old.getOrganizationName() : newOrgName.trim()
        );
    }

    public Contact rebuildPhones(Contact old, List<PhoneNumber> newPhones) {
        if (old instanceof PersonContact) {
            PersonContact p = (PersonContact) old;
            return new PersonContact(
                    p.getId(),
                    p.getOwnerUserId(),
                    p.getDisplayName(),
                    newPhones,
                    p.getEmailAddresses(),
                    p.getCreatedAt(),
                    LocalDateTime.now(),
                    p.getFirstName(),
                    p.getLastName()
            );
        } else if (old instanceof OrganizationContact) {
            OrganizationContact o = (OrganizationContact) old;
            return new OrganizationContact(
                    o.getId(),
                    o.getOwnerUserId(),
                    o.getDisplayName(),
                    newPhones,
                    o.getEmailAddresses(),
                    o.getCreatedAt(),
                    LocalDateTime.now(),
                    o.getOrganizationName()
            );
        }
        throw new IllegalArgumentException("Unsupported contact subtype: " + old.getClass());
    }

    public Contact rebuildEmails(Contact old, List<EmailAddress> newEmails) {
        if (old instanceof PersonContact) {
            PersonContact p = (PersonContact) old;
            return new PersonContact(
                    p.getId(),
                    p.getOwnerUserId(),
                    p.getDisplayName(),
                    p.getPhoneNumbers(),
                    newEmails,
                    p.getCreatedAt(),
                    LocalDateTime.now(),
                    p.getFirstName(),
                    p.getLastName()
            );
        } else if (old instanceof OrganizationContact) {
            OrganizationContact o = (OrganizationContact) old;
            return new OrganizationContact(
                    o.getId(),
                    o.getOwnerUserId(),
                    o.getDisplayName(),
                    o.getPhoneNumbers(),
                    newEmails,
                    o.getCreatedAt(),
                    LocalDateTime.now(),
                    o.getOrganizationName()
            );
        }
        throw new IllegalArgumentException("Unsupported contact subtype: " + old.getClass());
    }

    private String buildPersonDisplay(String first, String last, String fallback) {
        String f = first == null ? "" : first.trim();
        String l = last == null ? "" : last.trim();
        String combined = (f + " " + l).trim();
        return combined.isEmpty() ? fallback : combined;
    }
}