package services;

import domain.Contact;
import domain.EmailAddress;
import domain.OrganizationContact;
import domain.PersonContact;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FilterSortService {

    private final ContactRepository repository;

    public FilterSortService(ContactRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<Contact> filterAndSort(UUID ownerId,
                                       List<FilterStrategy<Contact>> filters,
                                       SortStrategy<Contact> sortStrategy) {
        List<Contact> base = repository.findAllByOwner(ownerId);
        if (base.isEmpty()) return List.of();

        // Apply filters (AND semantics)
        List<Contact> filtered = new ArrayList<>(base);
        for (FilterStrategy<Contact> f : filters) {
            filtered = filtered.stream().filter(f::test).collect(Collectors.toList());
            if (filtered.isEmpty()) break;
        }

        // Sort
        if (sortStrategy != null) {
            filtered.sort(sortStrategy.comparator());
        }
        return filtered;
    }

    // ---------- Built-in Filters (Strategy implementations) ----------
    public static final class Filters {

        public static final class HasEmail implements FilterStrategy<Contact> {
            @Override public boolean test(Contact c) {
                return c.getEmailAddresses() != null && !c.getEmailAddresses().isEmpty();
            }
            @Override public String toString() { return "HasEmail"; }
        }

        public static final class HasPhone implements FilterStrategy<Contact> {
            @Override public boolean test(Contact c) {
                return c.getPhoneNumbers() != null && !c.getPhoneNumbers().isEmpty();
            }
            @Override public String toString() { return "HasPhone"; }
        }

        public static final class TypePerson implements FilterStrategy<Contact> {
            @Override public boolean test(Contact c) { return c instanceof PersonContact; }
            @Override public String toString() { return "TypePerson"; }
        }

        public static final class TypeOrganization implements FilterStrategy<Contact> {
            @Override public boolean test(Contact c) { return c instanceof OrganizationContact; }
            @Override public String toString() { return "TypeOrganization"; }
        }

        public static final class CreatedLastNDays implements FilterStrategy<Contact> {
            private final int days;
            public CreatedLastNDays(int days) {
                if (days <= 0) throw new IllegalArgumentException("days must be > 0");
                this.days = days;
            }
            @Override public boolean test(Contact c) {
                LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
                return c.getCreatedAt().isAfter(cutoff);
            }
            @Override public String toString() { return "CreatedLastNDays(" + days + ")"; }
        }

        public static final class UpdatedLastNDays implements FilterStrategy<Contact> {
            private final int days;
            public UpdatedLastNDays(int days) {
                if (days <= 0) throw new IllegalArgumentException("days must be > 0");
                this.days = days;
            }
            @Override public boolean test(Contact c) {
                LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
                return c.getUpdatedAt().isAfter(cutoff);
            }
            @Override public String toString() { return "UpdatedLastNDays(" + days + ")"; }
        }

        public static final class EmailDomainContains implements FilterStrategy<Contact> {
            private final String partLower;
            public EmailDomainContains(String part) {
                if (part == null || part.isBlank()) throw new IllegalArgumentException("domain part required");
                this.partLower = part.toLowerCase(Locale.ROOT);
            }
            @Override public boolean test(Contact c) {
                for (EmailAddress ea : c.getEmailAddresses()) {
                    String addr = ea.getAddress();
                    if (addr == null) continue;
                    String lower = addr.toLowerCase(Locale.ROOT);
                    int at = lower.indexOf('@');
                    String domain = (at >= 0 && at + 1 < lower.length()) ? lower.substring(at + 1) : lower;
                    if (domain.contains(partLower)) return true;
                }
                return false;
            }
            @Override public String toString() { return "EmailDomainContains(" + partLower + ")"; }
        }

        public static final class NameContains implements FilterStrategy<Contact> {
            private final String needle;
            public NameContains(String needle) {
                if (needle == null || needle.isBlank()) throw new IllegalArgumentException("needle required");
                this.needle = needle.toLowerCase(Locale.ROOT);
            }
            @Override public boolean test(Contact c) {
                // displayName
                if (contains(ConsoleContactRenderer.bestEffortName(c), needle)) return true;
                if (c instanceof PersonContact p) {
                    if (contains(p.getFirstName(), needle)) return true;
                    if (contains(p.getLastName(), needle)) return true;
                }
                if (c instanceof OrganizationContact o) {
                    if (contains(o.getOrganizationName(), needle)) return true;
                }
                return false;
            }
            private boolean contains(String value, String needleLow) {
                return value != null && value.toLowerCase(Locale.ROOT).contains(needleLow);
            }
            @Override public String toString() { return "NameContains(" + needle + ")"; }
        }
    }
}