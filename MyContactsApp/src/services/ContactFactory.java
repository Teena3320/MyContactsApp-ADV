package services;

import domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static util.Validators.isNonBlank;
import static util.Validators.isValidEmail;
import static util.Validators.isValidPhone;

public final class ContactFactory {

    private ContactFactory() {}

    public static PersonBuilder person(UUID ownerUserId) {
        return new PersonBuilder(ownerUserId);
    }

    public static OrganizationBuilder organization(UUID ownerUserId) {
        return new OrganizationBuilder(ownerUserId);
    }

    public static final class PersonBuilder {
        private final UUID ownerUserId;
        private String firstName;
        private String lastName;
        private final List<PhoneNumber> phones = new ArrayList<>();
        private final List<EmailAddress> emails = new ArrayList<>();

        private PersonBuilder(UUID ownerUserId) {
            this.ownerUserId = Objects.requireNonNull(ownerUserId, "ownerUserId");
        }

        public PersonBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public PersonBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public PersonBuilder addPhone(PhoneType type, String number) {
            if (isNonBlank(number) && isValidPhone(number)) {
                phones.add(new PhoneNumber(Objects.requireNonNull(type), number.trim()));
            } else {
                throw new IllegalArgumentException("Invalid phone number.");
            }
            return this;
        }

        public PersonBuilder addEmail(EmailType type, String address) {
            String a = address == null ? null : address.trim().toLowerCase();
            if (isNonBlank(a) && isValidEmail(a)) {
                emails.add(new EmailAddress(Objects.requireNonNull(type), a));
            } else {
                throw new IllegalArgumentException("Invalid email address.");
            }
            return this;
        }

        public PersonContact build() {
            if (!isNonBlank(firstName) && !isNonBlank(lastName)) {
                throw new IllegalArgumentException("At least first name or last name must be provided.");
            }
            String display = (isNonBlank(firstName) ? firstName.trim() : "") +
                    (isNonBlank(lastName) ? (" " + lastName.trim()) : "");
            display = display.trim();
            if (display.isEmpty()) display = "Unnamed Person";

            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            return new PersonContact(id, ownerUserId, display, phones, emails, now, now, firstName, lastName);
        }
    }

    public static final class OrganizationBuilder {
        private final UUID ownerUserId;
        private String organizationName;
        private final List<PhoneNumber> phones = new ArrayList<>();
        private final List<EmailAddress> emails = new ArrayList<>();

        private OrganizationBuilder(UUID ownerUserId) {
            this.ownerUserId = Objects.requireNonNull(ownerUserId, "ownerUserId");
        }

        public OrganizationBuilder organizationName(String name) {
            this.organizationName = name;
            return this;
        }

        public OrganizationBuilder addPhone(PhoneType type, String number) {
            if (isNonBlank(number) && isValidPhone(number)) {
                phones.add(new PhoneNumber(Objects.requireNonNull(type), number.trim()));
            } else {
                throw new IllegalArgumentException("Invalid phone number.");
            }
            return this;
        }

        public OrganizationBuilder addEmail(EmailType type, String address) {
            String a = address == null ? null : address.trim().toLowerCase();
            if (isNonBlank(a) && isValidEmail(a)) {
                emails.add(new EmailAddress(Objects.requireNonNull(type), a));
            } else {
                throw new IllegalArgumentException("Invalid email address.");
            }
            return this;
        }

        public OrganizationContact build() {
            if (!isNonBlank(organizationName)) {
                throw new IllegalArgumentException("Organization name must be provided.");
            }
            String display = organizationName.trim();

            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            return new OrganizationContact(id, ownerUserId, display, phones, emails, now, now, organizationName);
        }
    }
}