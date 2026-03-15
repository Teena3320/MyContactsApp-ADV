package services;

import domain.*; 

import java.util.Locale;
import java.util.Objects;

public final class ContactSpecifications {

    private ContactSpecifications() {}

    public static Specification<Contact> nameContainsIgnoreCase(String part) {
        Objects.requireNonNull(part);
        String needle = part.toLowerCase(Locale.ROOT);
        return c -> {
            // displayName
            if (containsIgnoreCase(c.getDisplayName(), needle)) return true;
            // person fields
            if (c instanceof PersonContact p) {
                if (containsIgnoreCase(p.getFirstName(), needle)) return true;
                if (containsIgnoreCase(p.getLastName(), needle)) return true;
            }
            // org field
            if (c instanceof OrganizationContact o) {
                if (containsIgnoreCase(o.getOrganizationName(), needle)) return true;
            }
            return false;
        };
    }

    public static Specification<Contact> emailContainsIgnoreCase(String part) {
        Objects.requireNonNull(part);
        String needle = part.toLowerCase(Locale.ROOT);
        return c -> c.getEmailAddresses().stream()
                .map(EmailAddress::getAddress)
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .anyMatch(s -> s.contains(needle));
    }

    public static Specification<Contact> phoneContainsDigits(String digits) {
        Objects.requireNonNull(digits);
        String needle = digits.replaceAll("\\D+", "");
        if (needle.isEmpty()) {
            return Specification.alwaysTrue();
        }
        return c -> c.getPhoneNumbers().stream()
                .map(PhoneNumber::getNumber)
                .filter(Objects::nonNull)
                .map(s -> s.replaceAll("\\D+", ""))
                .anyMatch(s -> s.contains(needle));
    }

    public static Specification<Contact> typeIsPerson() {
        return c -> c instanceof PersonContact;
    }

    public static Specification<Contact> typeIsOrganization() {
        return c -> c instanceof OrganizationContact;
    }

    private static boolean containsIgnoreCase(String value, String needleLower) {
        if (value == null) return false;
        return value.toLowerCase(Locale.ROOT).contains(needleLower);
        }
}