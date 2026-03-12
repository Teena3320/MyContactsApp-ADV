package app;

import domain.User;
import domain.UserType;
import services.*;
import util.ValidationException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();
        PasswordHasher passwordHasher = new PasswordHasher();
        RegistrationService registrationService = new RegistrationService(userRepository, passwordHasher);

        AuthenticationStrategy basicStrategy = new BasicAuthStrategy(userRepository, passwordHasher);
        Map<AuthMethod, AuthenticationStrategy> strategies = new EnumMap<>(AuthMethod.class);
        strategies.put(AuthMethod.BASIC, basicStrategy);

        AuthService authService = new AuthService(strategies, SessionManager.getInstance());

        System.out.println("=== MyContacts App - UC-02  ===");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.println();
                System.out.println("1) Register new user");
                System.out.println("2) Login (Basic)");
                System.out.println("3) Who am I?");
                System.out.println("4) Logout");
                System.out.println("5) Exit");
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> handleRegistration(scanner, registrationService);
                    case "2" -> handleBasicLogin(scanner, authService);
                    case "3" -> handleWhoAmI(authService);
                    case "4" -> handleLogout(authService);
                    case "5" -> {
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