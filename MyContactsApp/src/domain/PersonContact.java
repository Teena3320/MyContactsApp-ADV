package domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PersonContact extends Contact {

    private final String firstName;
    private final String lastName;

    public PersonContact(UUID id,
                         UUID ownerUserId,
                         String displayName,
                         List<PhoneNumber> phoneNumbers,
                         List<EmailAddress> emailAddresses,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt,
                         String firstName,
                         String lastName) {
        super(id, ownerUserId, displayName, phoneNumbers, emailAddresses, createdAt, updatedAt);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    @Override
    public String toString() {
        return "PersonContact{" +
                "name='" + getDisplayName() + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phones=" + getPhoneNumbers() +
                ", emails=" + getEmailAddresses() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}