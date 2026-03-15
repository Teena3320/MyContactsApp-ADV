package services;

import domain.Contact;
import domain.OrganizationContact;
import domain.PersonContact;

import java.util.Comparator;
import java.util.Locale;

public enum SortStrategies implements SortStrategy<Contact> {

    NAME_ASC("Name ↑") {
        @Override public Comparator<Contact> comparator() {
            return Comparator.comparing(
                    c -> ConsoleContactRenderer.bestEffortName(c).toLowerCase(Locale.ROOT)
            );
        }
    },
    NAME_DESC("Name ↓") {
        @Override public Comparator<Contact> comparator() {
            return Comparator.comparing(
                    (Contact c) -> ConsoleContactRenderer.bestEffortName(c).toLowerCase(Locale.ROOT)
            ).reversed();
        }
    },
    CREATED_AT_ASC("Created ↑") {
        @Override public Comparator<Contact> comparator() {
            return Comparator.comparing(Contact::getCreatedAt);
        }
    },
    CREATED_AT_DESC("Created ↓") {
        @Override public Comparator<Contact> comparator() {
            return Comparator.comparing(Contact::getCreatedAt).reversed();
        }
    },
    UPDATED_AT_ASC("Updated ↑") {
        @Override public Comparator<Contact> comparator() {
            return Comparator.comparing(Contact::getUpdatedAt);
        }
    },
    UPDATED_AT_DESC("Updated ↓") {
        @Override public Comparator<Contact> comparator() {
            return Comparator.comparing(Contact::getUpdatedAt).reversed();
        }
    },
    TYPE_THEN_NAME("Type → Name") {
        @Override public Comparator<Contact> comparator() {
            return Comparator
                    .comparing(SortStrategies::typeOrder)
                    .thenComparing(c -> ConsoleContactRenderer.bestEffortName(c).toLowerCase(Locale.ROOT));
        }
    };

    private final String label;
    SortStrategies(String label) { this.label = label; }

    @Override
    public String label() { return label; }

    private static int typeOrder(Contact c) {
        if (c instanceof PersonContact) return 0;
        if (c instanceof OrganizationContact) return 1;
        return 2;
    }
}