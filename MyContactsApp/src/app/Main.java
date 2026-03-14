package app;

import domain.*;
import services.*;
import util.ValidationException;

import java.util.*;
/**
 * Use Case 4: Create Contact
 *
 *   This module enables:
 *   - Creating a new Person or Organization contact
 *   - Adding multiple phone numbers and email addresses
 *   - Capturing optional fields (address, tags, notes)
 *   - Storing timestamps and auto‑generated unique IDs
 *   Optional enhancements:
 *   - Auto‑validation for phone and email formats
 *   - Predefined tag suggestions (Family, Work, Friends)
 *
 *   Demonstrates:
 *   - Object construction using Builder Pattern
 *   - Contact inheritance hierarchy (PersonContact / OrganizationContact)
 *   - Composition (Contact has PhoneNumber, Email objects)
 *   - UUID generation for unique identifiers
 *   - Encapsulated validation and safe field initialization
 * 
 * @author tseb3003
 * @version 4.0
 */
public class Main {

    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();
        PasswordHasher passwordHasher = new PasswordHasher();
        RegistrationService registrationService = new RegistrationService(userRepository, passwordHasher);

        AuthenticationStrategy basicStrategy = new BasicAuthStrategy(userRepository, passwordHasher);
        Map<AuthMethod, AuthenticationStrategy> strategies = new EnumMap<>(AuthMethod.class);
        strategies.put(AuthMethod.BASIC, basicStrategy);
        AuthService authService = new AuthService(strategies, SessionManager.getInstance());

        CommandHistory history = new CommandHistory();

        // UC-04: Contacts
        ContactRepository contactRepository = new InMemoryContactRepository();
        ContactService contactService = new ContactService(contactRepository);

        System.out.println("=== MyContacts App - UC-01, UC-02 (Basic), UC-03 (Core), UC-04 (Create Contact) ===");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.println();
                System.out.println("1) Register new user");
                System.out.println("2) Login");
                System.out.println("3) View Profile");
                System.out.println("4) Update Full Name");
                System.out.println("5) Change Password");
                System.out.println("6) Undo last change");
                System.out.println("7) Redo change");
                System.out.println("8) Create Contact (Person)");
                System.out.println("9) Create Contact (Organization)");
                System.out.println("10) List My Contacts");
                System.out.println("11) Who am I?");
                System.out.println("12) Logout");
                System.out.println("13) Exit");
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> handleRegistration(scanner, registrationService);
                    case "2" -> handleBasicLogin(scanner, authService);
                    case "3" -> handleViewProfile(authService);
                    case "4" -> handleUpdateFullName(scanner, authService, userRepository, history);
                    case "5" -> handleChangePassword(scanner, authService, userRepository, passwordHasher, history);
                    case "6" -> System.out.println(history.undo());
                    case "7" -> System.out.println(history.redo());
                    case "8" -> handleCreatePerson(scanner, authService, contactService);
                    case "9" -> handleCreateOrganization(scanner, authService, contactService);
                    case "10" -> handleListContacts(authService, contactService);
                    case "11" -> handleWhoAmI(authService);
                    case "12" -> handleLogout(authService);
                    case "13" -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            }
        }
    }

    // ===== UC-01 =====
    private static void handleRegistration(Scanner scanner, RegistrationService registrationService) {
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("User type (FREE/PREMIUM): ");
        String typeRaw = scanner.nextLine().trim().toUpperCase();

        try {
            UserType type = UserType.valueOf(typeRaw);
            User user = registrationService.register(fullName, email, password, type);
            System.out.println("\nRegistration successful!");
            System.out.println("User ID: " + user.getId());
            System.out.println("Name   : " + user.getFullName());
            System.out.println("Email  : " + user.getEmail());
            System.out.println("Type   : " + type);
            System.out.println("Created: " + user.getCreatedAt());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid user type. Please enter FREE or PREMIUM.");
        } catch (ValidationException e) {
            System.out.println("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    // ===== UC-02 =====
    private static void handleBasicLogin(Scanner scanner, AuthService authService) {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> user = authService.login(AuthInput.basic(email, password));
        if (user.isPresent()) {
            System.out.println("Login successful. Welcome, " + user.get().getFullName() + "!");
        } else {
            System.out.println("Login failed. Invalid credentials.");
        }
    }

    private static void handleViewProfile(AuthService authService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }
        User u = current.get();
        String type = (u instanceof PremiumUser) ? "PREMIUM" : "FREE";
        System.out.println("\n=== Profile ===");
        System.out.println("Name       : " + u.getFullName());
        System.out.println("Email      : " + u.getEmail());
        System.out.println("Type       : " + type);
        System.out.println("Created    : " + u.getCreatedAt());
    }

    private static void handleUpdateFullName(Scanner scanner,
                                             AuthService authService,
                                             UserRepository repository,
                                             CommandHistory history) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }
        System.out.print("Enter new full name: ");
        String newName = scanner.nextLine().trim();

        try {
            ProfileCommand cmd = new UpdateFullNameCommand(repository, current.get(), newName);
            cmd.execute();
            history.record(cmd);
            System.out.println("Full name updated.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Update failed: " + ex.getMessage());
        }
    }

    private static void handleChangePassword(Scanner scanner,
                                             AuthService authService,
                                             UserRepository repository,
                                             PasswordHasher hasher,
                                             CommandHistory history) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }
        System.out.print("Enter current password: ");
        String currentPw = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPw = scanner.nextLine();

        try {
            ProfileCommand cmd = new ChangePasswordCommand(repository, hasher, current.get(), currentPw, newPw);
            cmd.execute();
            history.record(cmd);
            System.out.println("Password changed.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Change failed: " + ex.getMessage());
        }
    }

    private static void handleWhoAmI(AuthService authService) {
        Optional<User> current = authService.currentUser();
        if (current.isPresent()) {
            User u = current.get();
            System.out.println("Currently logged in as: " + u.getFullName() + " <" + u.getEmail() + ">");
        } else {
            System.out.println("No user is logged in.");
        }
    }

    private static void handleLogout(AuthService authService) {
        authService.logout();
        System.out.println("Logged out.");
    }

    // ===== UC-04 =====
    private static void handleCreatePerson(Scanner scanner,
                                           AuthService authService,
                                           ContactService contactService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }

        System.out.print("First name (optional): ");
        String first = emptyToNull(scanner.nextLine());
        System.out.print("Last name (optional): ");
        String last = emptyToNull(scanner.nextLine());

        List<PhoneNumber> phones = readPhones(scanner);
        List<EmailAddress> emails = readEmails(scanner);

        try {
            PersonContact c = contactService.createPerson(current.get().getId(), first, last, phones, emails);
            System.out.println("Person contact created with ID: " + c.getId());
        } catch (IllegalArgumentException ex) {
            System.out.println("Create failed: " + ex.getMessage());
        }
    }

    private static void handleCreateOrganization(Scanner scanner,
                                                 AuthService authService,
                                                 ContactService contactService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }

        System.out.print("Organization name: ");
        String org = scanner.nextLine().trim();

        List<PhoneNumber> phones = readPhones(scanner);
        List<EmailAddress> emails = readEmails(scanner);

        try {
            OrganizationContact c = contactService.createOrganization(current.get().getId(), org, phones, emails);
            System.out.println("Organization contact created with ID: " + c.getId());
        } catch (IllegalArgumentException ex) {
            System.out.println("Create failed: " + ex.getMessage());
        }
    }

    private static void handleListContacts(AuthService authService, ContactService contactService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }
        List<Contact> list = contactService.listMyContacts(current.get().getId());
        if (list.isEmpty()) {
            System.out.println("You have no contacts.");
            return;
        }
        System.out.println("\n=== My Contacts ===");
        for (Contact c : list) {
            System.out.println("- " + c.toString());
        }
    }

    private static List<PhoneNumber> readPhones(Scanner scanner) {
        List<PhoneNumber> phones = new ArrayList<>();
        while (true) {
            System.out.print("Add phone? (Y/N): ");
            String ans = scanner.nextLine().trim().toUpperCase();
            if (!ans.equals("Y")) break;

            PhoneType type = askPhoneType(scanner);
            System.out.print("Phone number: ");
            String number = scanner.nextLine().trim();
            phones.add(new PhoneNumber(type, number));
        }
        return phones;
    }

    private static PhoneType askPhoneType(Scanner scanner) {
        while (true) {
            System.out.print("Type (MOBILE/HOME/WORK/OTHER): ");
            String raw = scanner.nextLine().trim().toUpperCase();
            try {
                return PhoneType.valueOf(raw);
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid type. Try again.");
            }
        }
    }

    private static List<EmailAddress> readEmails(Scanner scanner) {
        List<EmailAddress> emails = new ArrayList<>();
        while (true) {
            System.out.print("Add email? (Y/N): ");
            String ans = scanner.nextLine().trim().toUpperCase();
            if (!ans.equals("Y")) break;

            EmailType type = askEmailType(scanner);
            System.out.print("Email address: ");
            String address = scanner.nextLine().trim();
            emails.add(new EmailAddress(type, address));
        }
        return emails;
    }

    private static EmailType askEmailType(Scanner scanner) {
        while (true) {
            System.out.print("Type (PERSONAL/WORK/OTHER): ");
            String raw = scanner.nextLine().trim().toUpperCase();
            try {
                return EmailType.valueOf(raw);
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid type. Try again.");
            }
        }
    }

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}