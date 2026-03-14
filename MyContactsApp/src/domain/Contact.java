package domain;

import java.time.LocalDateTime;
import java.util.*;

public abstract class Contact {
    private final UUID id;
    private final UUID ownerUserId;             
    private final String displayName;           
    private final List<PhoneNumber> phoneNumbers;
    private final List<EmailAddress> emailAddresses;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    protected Contact(UUID id,
                      UUID ownerUserId,
                      String displayName,
                      List<PhoneNumber> phoneNumbers,
                      List<EmailAddress> emailAddresses,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.ownerUserId = Objects.requireNonNull(ownerUserId, "ownerUserId");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.phoneNumbers = phoneNumbers == null ? List.of() : List.copyOf(phoneNumbers);
        this.emailAddresses = emailAddresses == null ? List.of() : List.copyOf(emailAddresses);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public UUID getId() { return id; }
    public UUID getOwnerUserId() { return ownerUserId; }
    public String getDisplayName() { return displayName; }
    public List<PhoneNumber> getPhoneNumbers() { return Collections.unmodifiableList(phoneNumbers); }
    public List<EmailAddress> getEmailAddresses() { return Collections.unmodifiableList(emailAddresses); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", phones=" + phoneNumbers +
                ", emails=" + emailAddresses +
                ", createdAt=" + createdAt +
                '}';
    }
}