package services;

import domain.Contact;
import domain.OrganizationContact;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EditOrganizationNameCommand implements ContactEditCommand {

    private final ContactEditService service;
    private final UUID contactId;
    private final String newOrganizationName;

    private OrganizationContact before;
    private OrganizationContact after;

    public EditOrganizationNameCommand(ContactEditService service,
                                       UUID contactId,
                                       String newOrganizationName) {
        this.service = Objects.requireNonNull(service);
        this.contactId = Objects.requireNonNull(contactId);
        this.newOrganizationName = newOrganizationName;
    }

    @Override
    public UUID contactId() {
        return contactId;
    }

    @Override
    public String execute() {
        Optional<Contact> found = service.findById(contactId);
        if (found.isEmpty() || !(found.get() instanceof OrganizationContact)) {
            return "Edit failed: contact not found or not an OrganizationContact.";
        }
        before = (OrganizationContact) found.get();
        after = service.rebuildOrganizationName(before, newOrganizationName);
        service.save(after);
        return "Updated organization name to: " + after.getOrganizationName();
    }

    @Override
    public String undo() {
        if (before == null) return "Nothing to undo.";
        service.save(before);
        return "Reverted organization name to: " + before.getOrganizationName();
    }

    @Override
    public String redo() {
        if (after == null) return "Nothing to redo.";
        service.save(after);
        return "Re-applied organization name: " + after.getOrganizationName();
    }
}