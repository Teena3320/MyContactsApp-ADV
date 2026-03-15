package services;

import domain.Contact;
import domain.PersonContact;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EditPersonNameCommand implements ContactEditCommand {

    private final ContactEditService service;
    private final UUID contactId;
    private final String newFirstOrNull;
    private final String newLastOrNull;

    private PersonContact before;
    private PersonContact after;

    public EditPersonNameCommand(ContactEditService service,
                                 UUID contactId,
                                 String newFirstOrNull,
                                 String newLastOrNull) {
        this.service = Objects.requireNonNull(service);
        this.contactId = Objects.requireNonNull(contactId);
        this.newFirstOrNull = newFirstOrNull;
        this.newLastOrNull = newLastOrNull;
    }

    @Override
    public UUID contactId() {
        return contactId;
    }

    @Override
    public String execute() {
        Optional<Contact> found = service.findById(contactId);
        if (found.isEmpty() || !(found.get() instanceof PersonContact)) {
            return "Edit failed: contact not found or not a PersonContact.";
        }
        before = (PersonContact) found.get();
        after = service.rebuildPerson(before, newFirstOrNull, newLastOrNull);
        service.save(after);
        return "Updated person name to: " + after.getDisplayName();
    }

    @Override
    public String undo() {
        if (before == null) return "Nothing to undo.";
        service.save(before);
        return "Reverted person name to: " + before.getDisplayName();
    }

    @Override
    public String redo() {
        if (after == null) return "Nothing to redo.";
        service.save(after);
        return "Re-applied person name: " + after.getDisplayName();
    }
}