package domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrganizationContact extends Contact {

    private final String organizationName;

    public OrganizationContact(UUID id,
                               UUID ownerUserId,
                               String displayName,
                               List<PhoneNumber> phoneNumbers,
                               List<EmailAddress> emailAddresses,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt,
                               String organizationName) {
        super(id, ownerUserId, displayName, phoneNumbers, emailAddresses, createdAt, updatedAt);
        this.organizationName = organizationName;
    }

    public String getOrganizationName() { return organizationName; }

    @Override
    public String toString() {
        return "OrganizationContact{" +
                "organizationName='" + organizationName + '\'' +
                ", phones=" + getPhoneNumbers() +
                ", emails=" + getEmailAddresses() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}