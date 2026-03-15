package services;

import domain.Contact;
import domain.EmailAddress;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ReplaceEmailsCommand implements ContactEditCommand {

    private final ContactEditService service;
    private final UUID contactId;
    private final List<EmailAddress> newEmails;

    private Contact before;
    private Contact after;

    public ReplaceEmailsCommand(ContactEditService service,
                                UUID contactId,
                                List<EmailAddress> newEmails) {
        this.service = Objects.requireNonNull(service);
        this.contactId = Objects.requireNonNull(contactId);
        this.newEmails = Objects.requireNonNull(newEmails);
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
        after = service.rebuildEmails(before, newEmails);
        service.save(after);
        return "Replaced email addresses (" + newEmails.size() + ").";
    }

    @Override
    public String undo() {
        if (before == null) return "Nothing to undo.";
        service.save(before);
        return "Restored previous email addresses (" + before.getEmailAddresses().size() + ").";
    }

    @Override
    public String redo() {
        if (after == null) return "Nothing to redo.";
        service.save(after);
        return "Re-applied email replacements (" + after.getEmailAddresses().size() + ").";
    }
}