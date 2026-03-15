package services;

import domain.Contact;
import domain.PhoneNumber;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ReplacePhonesCommand implements ContactEditCommand {

    private final ContactEditService service;
    private final UUID contactId;
    private final List<PhoneNumber> newPhones;

    private Contact before;
    private Contact after;

    public ReplacePhonesCommand(ContactEditService service,
                                UUID contactId,
                                List<PhoneNumber> newPhones) {
        this.service = Objects.requireNonNull(service);
        this.contactId = Objects.requireNonNull(contactId);
        this.newPhones = Objects.requireNonNull(newPhones);
    }

    @Override
    public UUID contactId() {
        return contactId;
    }

    @Override
    public String execute() {
        Optional<Contact> found = service.findById(contactId);
        if (found.isEmpty()) {
            return "Edit failed: contact not found.";
        }
        before = found.get();
        after = service.rebuildPhones(before, newPhones);
        service.save(after);
        return "Replaced phone numbers (" + newPhones.size() + ").";
    }

    @Override
    public String undo() {
        if (before == null) return "Nothing to undo.";
        service.save(before);
        return "Restored previous phone numbers (" + before.getPhoneNumbers().size() + ").";
    }

    @Override
    public String redo() {
        if (after == null) return "Nothing to redo.";
        service.save(after);
        return "Re-applied phone replacements (" + after.getPhoneNumbers().size() + ").";
    }
}