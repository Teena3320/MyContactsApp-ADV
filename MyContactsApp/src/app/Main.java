package app;

import domain.PremiumUser;
import domain.User;
import domain.UserType;
import services.*;
import util.ValidationException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
/**
 * UC-03: User Profile Management (Core Only: Name & Password)
 *
 * This section extends the console app to allow a logged-in user to manage core
 * profile details without UI preferences. It demonstrates:
 *
 *  - View Profile:
 *      Displays immutable identifiers (email, createdAt) and mutable fields (fullName).
 *
 *  - Update Full Name (Command Pattern):
 *      Uses UpdateFullNameCommand to validate non-blank names, persist the change
 *      via UserRepository, and support undo/redo through CommandHistory.
 *
 *  - Change Password (Security + Command Pattern):
 *      Uses ChangePasswordCommand to:
 *        * Verify the current password against the stored salted hash.
 *        * Enforce strong password policy (min length + upper/lower/digit/special).
 *        * Generate a new per-user salt and hash (via PasswordHasher) before persisting.
 *      Prior password material (salt + hash) is captured to support undo.
 *
 *  - Undo/Redo:
 *      CommandHistory maintains stacks for undo/redo, enabling reversible
 *      profile changes while keeping side effects (repository saves) consistent.
 *
 * Key OOP & Design Concepts:
 *  - Encapsulation: User keeps password salt/hash private; updates happen through controlled methods.
 *  - Abstraction: ProfileCommand interface abstracts execute/undo contract for profile actions.
 *  - Command Pattern: Each profile change is a self-contained command (execute/undo/description).
 *  - Repository Abstraction: UserRepository decouples persistence (here, in-memory) from domain logic.
 *  - Validation & Exceptions: Clear validation (name non-blank, strong password) with actionable messages.
 *
 * Notes:
 *  - Password hashing uses salted SHA-256 for demonstration. For production, use a KDF
 *    such as PBKDF2, bcrypt, scrypt, or Argon2.
 *  - Email is treated as an immutable identifier post registration.
 *
 * @author tseb3003
 * @version 3.0
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

        System.out.println("=== MyContacts App - UC-03 ===");

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
                System.out.println("8) Who am I?");
                System.out.println("9) Logout");
                System.out.println("10) Exit");
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
                    case "8" -> handleWhoAmI(authService);
                    case "9" -> handleLogout(authService);
                    case "10" -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            }
        }
    }

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
}