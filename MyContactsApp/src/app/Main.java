package app;

import domain.*;
import services.*;
import util.ValidationException;
import java.util.*;
/**
 * Use Case 6: Edit Contact
 *
 *   This module enables:
 *   - Selecting an existing contact belonging to the logged‑in user
 *   - Modifying key attributes (names, organization fields, phones, emails)
 *   - Persisting updated immutable Contact objects through safe reconstruction
 *   Optional enhancements:
 *   - Full Undo/Redo support using the Command Pattern
 *   - Granular editing commands (name update, phone replace, email replace)
 *
 *   Demonstrates:
 *   - Controlled mutation through immutable object rebuilding
 *   - Command Pattern for reversible edit operations
 *   - Memento‑style state preservation for undo/redo stacks
 *   - Polymorphic update handling for PersonContact / OrganizationContact
 *   - Clean separation of concerns using ContactEditService and edit commands
 *
 * @author tseb3003
 * @version 6.0
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

     ContactRepository contactRepository = new InMemoryContactRepository();
     ContactService contactService = new ContactService(contactRepository);

     ContactRenderer renderer = new ConsoleContactRenderer();

     ContactEditService editService = new ContactEditService(contactRepository);
     ContactCommandHistory contactHistory = new ContactCommandHistory();

     System.out.println("=== MyContacts App - UC-06 ===");

     try (Scanner scanner = new Scanner(System.in)) {
         boolean running = true;
         while (running) {
             System.out.println();
             System.out.println("1)  Register new user");
             System.out.println("2)  Login");
             System.out.println("3)  View Profile");
             System.out.println("4)  Update Full Name");
             System.out.println("5)  Change Password");
             System.out.println("6)  Undo last profile change");
             System.out.println("7)  Redo profile change");
             System.out.println("8)  Create Contact ");
             System.out.println("9)  Create Contact");
             System.out.println("10) List My Contacts");
             System.out.println("11) View Contact Details ");
             System.out.println("12) Edit Contact ");
             System.out.println("13) Undo last contact edit ");
             System.out.println("14) Redo contact edit ");
             System.out.println("15) Who am I?");
             System.out.println("16) Logout");
             System.out.println("17) Exit");
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
                 case "11" -> handleViewContactDetails(scanner, authService, contactService, renderer);
                 case "12" -> handleEditContact(scanner, authService, contactService, editService, contactHistory);
                 case "13" -> System.out.println(contactHistory.undo());
                 case "14" -> System.out.println(contactHistory.redo());
                 case "15" -> handleWhoAmI(authService);
                 case "16" -> handleLogout(authService);
                 case "17" -> {
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

 private static void handleViewContactDetails(Scanner scanner,
                                              AuthService authService,
                                              ContactService contactService,
                                              ContactRenderer renderer) {
     Optional<User> current = authService.currentUser();
     if (current.isEmpty()) {
         System.out.println("Please login first.");
         return;
     }
     UUID userId = current.get().getId();

     List<Contact> contacts = contactService.listMyContacts(userId);
     if (contacts.isEmpty()) {
         System.out.println("You have no contacts to view.");
         return;
     }

     System.out.println("\n=== Select a Contact ===");
     for (int i = 0; i < contacts.size(); i++) {
         Contact c = contacts.get(i);
         System.out.printf("%2d) %s [id=%s]%n", i + 1, summarize(c), c.getId());
     }
     System.out.print("Enter number or paste Contact ID: ");
     String sel = scanner.nextLine().trim();

     Optional<Contact> chosen = chooseContactFromInput(contacts, sel);
     if (chosen.isEmpty()) {
         System.out.println("Invalid selection.");
         return;
     }

     boolean uppercase = askYesNo(scanner, "Uppercase name? (Y/N): ");
     boolean maskEmails = askYesNo(scanner, "Mask emails? (Y/N): ");

     ContactRendererOptions options = ContactRendererOptions.builder()
             .uppercaseName(uppercase)
             .maskEmails(maskEmails)
             .build();

     String rendered = renderer.render(chosen.get(), options);
     System.out.println(rendered);
 }

 private static void handleEditContact(Scanner scanner,
                                       AuthService authService,
                                       ContactService contactService,
                                       ContactEditService editService,
                                       ContactCommandHistory contactHistory) {
     Optional<User> current = authService.currentUser();
     if (current.isEmpty()) {
         System.out.println("Please login first.");
         return;
     }
     UUID userId = current.get().getId();

     List<Contact> contacts = contactService.listMyContacts(userId);
     if (contacts.isEmpty()) {
         System.out.println("You have no contacts to edit.");
         return;
     }

     System.out.println("\n=== Select a Contact to Edit ===");
     for (int i = 0; i < contacts.size(); i++) {
         Contact c = contacts.get(i);
         System.out.printf("%2d) %s [id=%s]%n", i + 1, summarize(c), c.getId());
     }
     System.out.print("Enter number or paste Contact ID: ");
     String sel = scanner.nextLine().trim();

     Optional<Contact> chosenOpt = chooseContactFromInput(contacts, sel);
     if (chosenOpt.isEmpty()) {
         System.out.println("Invalid selection.");
         return;
     }
     Contact chosen = chosenOpt.get();

     if (chosen instanceof PersonContact) {
         System.out.println("\nEdit Person Contact:");
         System.out.println("1) Update first/last name");
         System.out.println("2) Replace phone numbers");
         System.out.println("3) Replace email addresses");
         System.out.print("Choose: ");
         String csel = scanner.nextLine().trim();
         switch (csel) {
             case "1" -> {
                 System.out.print("New first name (blank to keep): ");
                 String first = scanner.nextLine();
                 System.out.print("New last name  (blank to keep): ");
                 String last = scanner.nextLine();
                 EditPersonNameCommand cmd = new EditPersonNameCommand(
                         editService, chosen.getId(),
                         blankToNull(first), blankToNull(last)
                 );
                 System.out.println(contactHistory.recordAndExecute(cmd));
             }
             case "2" -> {
                 List<PhoneNumber> phones = readPhones(scanner);
                 ReplacePhonesCommand cmd = new ReplacePhonesCommand(editService, chosen.getId(), phones);
                 System.out.println(contactHistory.recordAndExecute(cmd));
             }
             case "3" -> {
                 List<EmailAddress> emails = readEmails(scanner);
                 ReplaceEmailsCommand cmd = new ReplaceEmailsCommand(editService, chosen.getId(), emails);
                 System.out.println(contactHistory.recordAndExecute(cmd));
             }
             default -> System.out.println("Invalid choice.");
         }
     } else if (chosen instanceof OrganizationContact) {
         System.out.println("\nEdit Organization Contact:");
         System.out.println("1) Update organization name");
         System.out.println("2) Replace phone numbers");
         System.out.println("3) Replace email addresses");
         System.out.print("Choose: ");
         String csel = scanner.nextLine().trim();
         switch (csel) {
             case "1" -> {
                 System.out.print("New organization name: ");
                 String org = scanner.nextLine().trim();
                 EditOrganizationNameCommand cmd = new EditOrganizationNameCommand(editService, chosen.getId(), org);
                 System.out.println(contactHistory.recordAndExecute(cmd));
             }
             case "2" -> {
                 List<PhoneNumber> phones = readPhones(scanner);
                 ReplacePhonesCommand cmd = new ReplacePhonesCommand(editService, chosen.getId(), phones);
                 System.out.println(contactHistory.recordAndExecute(cmd));
             }
             case "3" -> {
                 List<EmailAddress> emails = readEmails(scanner);
                 ReplaceEmailsCommand cmd = new ReplaceEmailsCommand(editService, chosen.getId(), emails);
                 System.out.println(contactHistory.recordAndExecute(cmd));
             }
             default -> System.out.println("Invalid choice.");
         }
     } else {
         System.out.println("Unknown contact type.");
     }
 }


 private static Optional<Contact> chooseContactFromInput(List<Contact> contacts, String sel) {
     Optional<Contact> chosen = Optional.empty();
     try {
         UUID id = UUID.fromString(sel);
         chosen = contacts.stream().filter(c -> c.getId().equals(id)).findFirst();
     } catch (IllegalArgumentException ignored) {
         try {
             int idx = Integer.parseInt(sel);
             if (idx >= 1 && idx <= contacts.size()) {
                 chosen = Optional.of(contacts.get(idx - 1));
             }
         } catch (NumberFormatException ignored2) {
         }
     }
     return chosen;
 }

 private static boolean askYesNo(Scanner scanner, String prompt) {
     while (true) {
         System.out.print(prompt);
         String a = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
         if (a.equals("Y")) return true;
         if (a.equals("N")) return false;
         System.out.println("Please enter Y or N.");
     }
 }

 private static String summarize(Contact c) {
     String name = ConsoleContactRenderer.bestEffortName(c);
     String kind = c.getClass().getSimpleName();
     return name + " (" + kind + ")";
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

 private static String blankToNull(String s) {
     return (s == null || s.trim().isEmpty()) ? null : s.trim();
 }
}