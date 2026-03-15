package services;

import domain.Contact;
import domain.EmailAddress;
import domain.PhoneNumber;

import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ConsoleContactRenderer implements ContactRenderer {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String render(Contact contact, ContactRendererOptions options) {
        StringBuilder sb = new StringBuilder();

        String name = bestEffortName(contact);
        if (options.uppercaseName()) {
            name = name.toUpperCase(Locale.ROOT);
        }

        sb.append("========================================\n");
        sb.append("Contact Details\n");
        sb.append("========================================\n");
        sb.append("ID        : ").append(safeId(contact)).append("\n");
        sb.append("Type      : ").append(contact.getClass().getSimpleName()).append("\n");
        sb.append("Name      : ").append(name).append("\n");

        Map<String, String> extras = bestEffortExtras(contact);
        for (Map.Entry<String, String> e : extras.entrySet()) {
            sb.append(String.format("%-10s: %s%n", e.getKey(), e.getValue()));
        }

        List<PhoneNumber> phones = bestEffortPhones(contact);
        if (phones.isEmpty()) {
            sb.append("Phones    : (none)\n");
        } else {
            sb.append("Phones    :\n");
            for (PhoneNumber p : phones) {
                String label = safeEnumName(invokeEnum(p, "getType"));
                String number = invokeString(p, "getNumber");
                sb.append("  - ").append(label).append(": ").append(number).append("\n");
            }
        }

        List<EmailAddress> emails = bestEffortEmails(contact);
        if (emails.isEmpty()) {
            sb.append("Emails    : (none)\n");
        } else {
            sb.append("Emails    :\n");
            for (EmailAddress e : emails) {
                String label = safeEnumName(invokeEnum(e, "getType"));
                String address = invokeString(e, "getAddress");
                if (options.maskEmails() && address != null) address = maskEmail(address);
                sb.append("  - ").append(label).append(": ").append(address).append("\n");
            }
        }

        String created = bestEffortTime(contact, "getCreatedAt");
        String updated = bestEffortTime(contact, "getUpdatedAt");
        if (created != null) sb.append("Created   : ").append(created).append("\n");
        if (updated != null) sb.append("Updated   : ").append(updated).append("\n");

        sb.append("========================================\n");
        return sb.toString();
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) return local.charAt(0) + "*" + domain;
        StringBuilder masked = new StringBuilder();
        masked.append(local.charAt(0));
        for (int i = 1; i < local.length() - 1; i++) masked.append('*');
        masked.append(local.charAt(local.length() - 1));
        return masked + domain;
    }

    private static UUID safeId(Contact c) {
        try {
            Method m = c.getClass().getMethod("getId");
            Object v = m.invoke(c);
            return (UUID) v;
        } catch (Exception e) {
            return null;
        }
    }

    public static String bestEffortName(Contact c) {
        List<String> methods = Arrays.asList(
                "getFullName", "fullName",
                "getDisplayName", "displayName",
                "getName", "name",
                "getOrganizationName", "organizationName"
        );
        for (String m : methods) {
            String val = invokeString(c, m);
            if (val != null && !val.trim().isEmpty()) return val.trim();
        }
        String first = invokeString(c, "getFirstName");
        String last = invokeString(c, "getLastName");
        if ((first != null && !first.isBlank()) || (last != null && !last.isBlank())) {
            return ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        }
        // Fallback
        return String.valueOf(c);
    }

    private static Map<String, String> bestEffortExtras(Contact c) {
        Map<String, String> extras = new LinkedHashMap<>();
        // Person-ish fields
        String first = invokeString(c, "getFirstName");
        if (notBlank(first)) extras.put("First Name", first);
        String last = invokeString(c, "getLastName");
        if (notBlank(last)) extras.put("Last Name", last);
        // Org-ish fields
        String legal = invokeString(c, "getLegalName");
        if (notBlank(legal)) extras.put("Legal Name", legal);
        String reg = invokeString(c, "getRegistrationId");
        if (notBlank(reg)) extras.put("Reg. ID", reg);
        return extras;
    }

    @SuppressWarnings("unchecked")
    private static List<PhoneNumber> bestEffortPhones(Contact c) {
        // Try getPhones(), getPhoneNumbers()
        List<String> candidates = Arrays.asList("getPhones", "getPhoneNumbers");
        for (String m : candidates) {
            Object v = invoke(c, m);
            if (v instanceof List<?>) {
                List<?> raw = (List<?>) v;
                if (!raw.isEmpty() && raw.get(0) instanceof PhoneNumber) {
                    return (List<PhoneNumber>) v;
                }
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static List<EmailAddress> bestEffortEmails(Contact c) {
        // Try getEmails(), getEmailAddresses()
        List<String> candidates = Arrays.asList("getEmails", "getEmailAddresses");
        for (String m : candidates) {
            Object v = invoke(c, m);
            if (v instanceof List<?>) {
                List<?> raw = (List<?>) v;
                if (!raw.isEmpty() && raw.get(0) instanceof EmailAddress) {
                    return (List<EmailAddress>) v;
                }
            }
        }
        return Collections.emptyList();
    }

    private static String bestEffortTime(Contact c, String method) {
        Object v = invoke(c, method);
        if (v == null) return null;
        try {
            Method fmt = v.getClass().getMethod("format", java.time.format.DateTimeFormatter.class);
            Object s = fmt.invoke(v, TS_FMT);
            return String.valueOf(s);
        } catch (Exception ignored) {
            return String.valueOf(v);
        }
    }

    private static Object invoke(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            return m.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    static String invokeString(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            Object v = m.invoke(target);
            return v == null ? null : String.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    static Enum<?> invokeEnum(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            Object v = m.invoke(target);
            return (v instanceof Enum<?>) ? (Enum<?>) v : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String safeEnumName(Enum<?> e) {
        return e == null ? "(unknown)" : e.name();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}