package services;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public final class ContactLeaf implements GroupMember {

    private final UUID contactId;

    public ContactLeaf(UUID contactId) {
        this.contactId = contactId;
    }

    public UUID contactId() { return contactId; }

    @Override
    public Set<UUID> flattenContactIds() {
        return Collections.singleton(contactId);
    }
}