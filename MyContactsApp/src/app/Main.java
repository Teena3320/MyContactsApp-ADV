package app;

import domain.User;
import domain.UserType;
import services.*;
import util.ValidationException;

import java.util.Scanner;
/**
 * UC-01: User Registration
 *
 * This main function demonstrates the execution flow for the User Registration
 * use case in the MyContacts App. It initializes the required service components
 * (UserRepository, PasswordHasher, RegistrationService) and provides a simple
 * console-driven interface for:
 *
 *  - Accepting user input: full name, email, password, and user type.
 *  - Validating user data (email format, strong password, non-empty fields).
 *  - Checking for uniqueness of the email using the UserRepository.
 *  - Creating the appropriate User object (FreeUser or PremiumUser) through
 *    the Factory + Builder patterns.
 *  - Hashing and salting the password before user creation.
 *  - Persisting the new user into the in-memory repository.
 *
 * The purpose of this method is to simulate how the application handles the
 * full registration workflow while demonstrating OOP principles such as:
 * encapsulation, abstraction, design patterns (Factory, Builder), input
 * validation, and exception handling.
 *
 * @author tseb3003
 * @version 1.0
 */

public class Main {

    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();
        PasswordHasher passwordHasher = new PasswordHasher();
        RegistrationService registrationService = new RegistrationService(userRepository, passwordHasher);

        System.out.println("=== MyContacts App - UC-01: User Registration ===");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.println();
                System.out.println("1) Register new user");
                System.out.println("2) Exit");
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> {
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
                    case "2" -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            }
        }
    }
}