package app;

import domain.*;
import services.*;
import util.ValidationException;

import java.util.*;
/**
 * Use Case 11: Create and Manage Tags
 *
 *   This module enables:
 *   - Creating owner‑scoped tags with normalized names (case‑insensitive)
 *   - Assigning and removing tags to/from contacts (many‑to‑many)
 *   - Listing all tags and querying contacts by a given tag
 *   Optional enhancements:
 *   - Tag rename/merge operations and duplicate resolution
 *   - Visual attributes (color/emoji) for UI emphasis
 *   - Predefined tag sets (e.g., Enum‑backed starter categories)
 *
 *   Demonstrates:
 *   - Flyweight‑style reuse of Tag value objects per owner
 *   - Many‑to‑many association management (Contact ↔ Tag)
 *   - Robust normalization and equality semantics for value objects
 *   - Clear separation of concerns: tag catalog vs. contact‑tag links
 *   - Safe access patterns (immutable views of tag collections)
 *
 *   Components:
 *   - Tag            : Value object (normalized name, display label)
 *   - TagService     : Owner‑scoped registry + contact‑tag associations
 *   - Integrations   : Works alongside ContactService and view rendering
 *
 *   Notes:
 *   - All tag operations are scoped to the logged‑in owner (user).
 *   - Normalization ensures "Work" and "work" refer to the same Tag.
 *
 * @author tseb3003
 * @version 11.0
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

        InMemoryContactRepository contactRepository = new InMemoryContactRepository();
        ContactService contactService = new ContactService(contactRepository);

        ContactRenderer renderer = new ConsoleContactRenderer();

        ContactEditService editService = new ContactEditService(contactRepository);
        ContactCommandHistory contactHistory = new ContactCommandHistory();

        ContactDeletionService deletionService = new ContactDeletionService(contactRepository);

        GroupService groupService = new GroupService(contactRepository);

        SearchService searchService = new SearchService(contactRepository);

        FilterSortService filterSortService = new FilterSortService(contactRepository);

        TagService tagService = new TagService(contactRepository);

        System.out.println("=== MyContacts App - UC-11 ===");

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
                System.out.println("8)  Create Contact (Person)");
                System.out.println("9)  Create Contact (Organization)");
                System.out.println("10) List My Contacts");
                System.out.println("11) View Contact Details ");
                System.out.println("12) Edit Contact ");
                System.out.println("13) Undo last contact edit");
                System.out.println("14) Redo contact edit ");
                System.out.println("15) Delete Contact ");
                System.out.println("16) View Trash ");
                System.out.println("17) Restore from Trash ");
                System.out.println("18) Purge from Trash ");
                System.out.println("19) Create Group ");
                System.out.println("20) Add Contact to Group ");
                System.out.println("21) Remove Contact from Group");
                System.out.println("22) List Groups ");
                System.out.println("23) Bulk Soft Delete Group ");
                System.out.println("24) Bulk Export Group ");
                System.out.println("25) Search Contacts ");
                System.out.println("26) Advanced Filter & Sort ");
                System.out.println("27) Tag: Create ");
                System.out.println("28) Tag: List All");
                System.out.println("29) Tag: Assign to Contact");
                System.out.println("30) Tag: Remove from Contact ");
                System.out.println("31) Tag: Show Contact's Tags");
                System.out.println("32) Tag: List Contacts by Tag ");
                System.out.println("33) Who am I?");
                System.out.println("34) Logout");
                System.out.println("35) Exit");
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
                    case "11" -> handleViewContactDetails(scanner, authService, contactService, renderer, tagService);
                    case "12" -> handleEditContact(scanner, authService, contactService, editService, contactHistory);
                    case "13" -> System.out.println(contactHistory.undo());
                    case "14" -> System.out.println(contactHistory.redo());
                    case "15" -> handleDeleteContact(scanner, authService, contactService, deletionService);
                    case "16" -> handleViewTrash(scanner, authService, deletionService);
                    case "17" -> handleRestoreFromTrash(scanner, authService, deletionService);
                    case "18" -> handlePurgeFromTrash(scanner, authService, deletionService);
                    case "19" -> handleCreateGroup(scanner, authService, groupService);
                    case "20" -> handleAddContactToGroup(scanner, authService, contactService, groupService);
                    case "21" -> handleRemoveContactFromGroup(scanner, authService, groupService);
                    case "22" -> handleListGroups(scanner, authService, groupService);
                    case "23" -> handleBulkSoftDeleteGroup(scanner, authService, groupService, deletionService);
                    case "24" -> handleBulkExportGroup(scanner, authService, groupService, contactRepository, renderer);
                    case "25" -> handleSearchContacts(scanner, authService, searchService, renderer);
                    case "26" -> handleAdvancedFilterSort(scanner, authService, filterSortService, renderer);
                    case "27" -> handleCreateTag(scanner, authService, tagService);
                    case "28" -> handleListTags(scanner, authService, tagService);
                    case "29" -> handleAssignTagToContact(scanner, authService, contactService, tagService);
                    case "30" -> handleRemoveTagFromContact(scanner, authService, contactService, tagService);
                    case "31" -> handleShowContactTags(scanner, authService, contactService, tagService);
                    case "32" -> handleListContactsByTag(scanner, authService, contactService, tagService);
                    case "33" -> handleWhoAmI(authService);
                    case "34" -> handleLogout(authService);
                    case "35" -> {
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

    // ===== UC-05 =====
    private static void handleViewContactDetails(Scanner scanner,
                                                 AuthService authService,
                                                 ContactService contactService,
                                                 ContactRenderer renderer,
                                                 TagService tagService) {
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

        // Show tags (UC-11)
        Set<Tag> tags = tagService.tagsForContact(userId, chosen.get().getId());
        if (tags.isEmpty()) {
            System.out.println("Tags     : (none)");
        } else {
            System.out.print("Tags     : ");
            boolean first = true;
            for (Tag t : tags) {
                if (!first) System.out.print(", ");
                System.out.print(t.getDisplay());
                first = false;
            }
            System.out.println();
        }
        System.out.println("========================================");
    }

    // ===== UC-06 =====
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

    // ===== UC-07 =====
    private static void handleDeleteContact(Scanner scanner,
                                            AuthService authService,
                                            ContactService contactService,
                                            ContactDeletionService deletionService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }
        UUID ownerId = current.get().getId();

        List<Contact> contacts = contactService.listMyContacts(ownerId);
        if (contacts.isEmpty()) {
            System.out.println("You have no contacts to delete.");
            return;
        }

        System.out.println("\n=== Select a Contact to Delete ===");
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

        boolean soft = askYesNo(scanner, "Soft delete (Y) or Hard delete (N)? ");
        String confirm = prompt(scanner, "Type DELETE to confirm: ");
        if (!"DELETE".equalsIgnoreCase(confirm.trim())) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (soft) {
            boolean ok = deletionService.softDelete(ownerId, chosen.getId());
            System.out.println(ok ? "Contact moved to Trash." : "Soft delete failed.");
        } else {
            boolean ok = deletionService.hardDelete(chosen.getId());
            System.out.println(ok ? "Contact permanently deleted." : "Hard delete failed.");
        }
    }

    private static void handleViewTrash(Scanner scanner,
                                        AuthService authService,
                                        ContactDeletionService deletionService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }
        UUID ownerId = current.get().getId();

        List<DeletedContact> trash = deletionService.listTrash(ownerId);
        if (trash.isEmpty()) {
            System.out.println("Trash is empty.");
            return;
        }
        System.out.println("\n=== Trash ===");
        for (int i = 0; i < trash.size(); i++) {
            DeletedContact dc = trash.get(i);
            System.out.printf("%2d) %s [id=%s] deletedAt=%s%n",
                    i + 1,
                    ConsoleContactRenderer.bestEffortName(dc.contact()),
                    dc.contact().getId(),
                    dc.deletedAt()
            );
        }
    }

    private static void handleRestoreFromTrash(Scanner scanner,
                                               AuthService authService,
                                               ContactDeletionService deletionService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }
        UUID ownerId = current.get().getId();

        List<DeletedContact> trash = deletionService.listTrash(ownerId);
        if (trash.isEmpty()) {
            System.out.println("Trash is empty.");
            return;
        }
        for (int i = 0; i < trash.size(); i++) {
            DeletedContact dc = trash.get(i);
            System.out.printf("%2d) %s [id=%s]%n",
                    i + 1, ConsoleContactRenderer.bestEffortName(dc.contact()), dc.contact().getId());
        }
        System.out.print("Enter number or paste Contact ID to restore: ");
        String sel = scanner.nextLine().trim();

        Optional<DeletedContact> chosen = chooseDeletedFromInput(trash, sel);
        if (chosen.isEmpty()) {
            System.out.println("Invalid selection.");
            return;
        }
        boolean ok = deletionService.restore(ownerId, chosen.get().contact().getId());
        System.out.println(ok ? "Contact restored." : "Restore failed.");
    }

    private static void handlePurgeFromTrash(Scanner scanner,
                                             AuthService authService,
                                             ContactDeletionService deletionService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) {
            System.out.println("Please login first.");
            return;
        }
        UUID ownerId = current.get().getId();

        List<DeletedContact> trash = deletionService.listTrash(ownerId);
        if (trash.isEmpty()) {
            System.out.println("Trash is empty.");
            return;
        }
        for (int i = 0; i < trash.size(); i++) {
            DeletedContact dc = trash.get(i);
            System.out.printf("%2d) %s [id=%s]%n",
                    i + 1, ConsoleContactRenderer.bestEffortName(dc.contact()), dc.contact().getId());
        }
        System.out.print("Enter number or paste Contact ID to purge permanently: ");
        String sel = scanner.nextLine().trim();

        Optional<DeletedContact> chosen = chooseDeletedFromInput(trash, sel);
        if (chosen.isEmpty()) {
            System.out.println("Invalid selection.");
            return;
        }
        String confirm = prompt(scanner, "Type PURGE to confirm: ");
        if (!"PURGE".equalsIgnoreCase(confirm.trim())) {
            System.out.println("Purge cancelled.");
            return;
        }
        boolean ok = deletionService.purge(ownerId, chosen.get().contact().getId());
        System.out.println(ok ? "Contact purged from Trash." : "Purge failed.");
    }

    // ===== UC-08 =====
    private static void handleCreateGroup(Scanner scanner,
                                          AuthService authService,
                                          GroupService groupService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }

        System.out.print("Group name: ");
        String name = scanner.nextLine().trim();
        boolean ok = groupService.createGroup(current.get().getId(), name);
        System.out.println(ok ? "Group created." : "Group already exists.");
    }

    private static void handleAddContactToGroup(Scanner scanner,
                                                AuthService authService,
                                                ContactService contactService,
                                                GroupService groupService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        List<String> groups = groupService.listGroupNames(ownerId);
        if (groups.isEmpty()) { System.out.println("No groups. Create one first."); return; }
        printGroups(groups);

        String groupName = chooseGroupNameFromInput(scanner, groups);
        if (groupName == null) { System.out.println("Invalid group selection."); return; }

        List<Contact> contacts = contactService.listMyContacts(ownerId);
        if (contacts.isEmpty()) { System.out.println("You have no contacts."); return; }
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%2d) %s (%s) [id=%s]%n", i + 1, ConsoleContactRenderer.bestEffortName(c),
                    c.getClass().getSimpleName(), c.getId());
        }
        System.out.print("Enter number or paste Contact ID: ");
        String sel = scanner.nextLine().trim();

        Optional<Contact> chosen = chooseContactFromInput(contacts, sel);
        if (chosen.isEmpty()) { System.out.println("Invalid selection."); return; }

        boolean ok = groupService.addContact(ownerId, groupName, chosen.get().getId());
        System.out.println(ok ? "Contact added to group." : "Add failed (missing group or ownership mismatch).");
    }

    private static void handleRemoveContactFromGroup(Scanner scanner,
                                                     AuthService authService,
                                                     GroupService groupService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        List<String> groups = groupService.listGroupNames(ownerId);
        if (groups.isEmpty()) { System.out.println("No groups."); return; }
        printGroups(groups);

        String groupName = chooseGroupNameFromInput(scanner, groups);
        if (groupName == null) { System.out.println("Invalid group selection."); return; }

        Set<UUID> ids = groupService.flattenContactIds(ownerId, groupName);
        if (ids.isEmpty()) { System.out.println("Group is empty."); return; }
        List<UUID> asList = new ArrayList<>(ids);
        for (int i = 0; i < asList.size(); i++) {
            System.out.printf("%2d) %s%n", i + 1, asList.get(i));
        }
        System.out.print("Enter number or paste Contact ID to remove: ");
        String sel = scanner.nextLine().trim();

        Optional<UUID> chosen = chooseUuidFromInput(asList, sel);
        if (chosen.isEmpty()) { System.out.println("Invalid selection."); return; }

        boolean ok = groupService.removeContact(ownerId, groupName, chosen.get());
        System.out.println(ok ? "Contact removed from group." : "Remove failed.");
    }

    private static void handleListGroups(Scanner scanner,
                                         AuthService authService,
                                         GroupService groupService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        List<String> groups = groupService.listGroupNames(ownerId);
        if (groups.isEmpty()) {
            System.out.println("No groups.");
            return;
        }
        System.out.println("\n=== Groups ===");
        for (int i = 0; i < groups.size(); i++) {
            String g = groups.get(i);
            int size = groupService.flattenContactIds(ownerId, g).size();
            System.out.printf("%2d) %s (%d contacts)%n", i + 1, g, size);
        }
    }

    private static void handleBulkSoftDeleteGroup(Scanner scanner,
                                                  AuthService authService,
                                                  GroupService groupService,
                                                  ContactDeletionService deletionService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        List<String> groups = groupService.listGroupNames(ownerId);
        if (groups.isEmpty()) { System.out.println("No groups."); return; }
        printGroups(groups);

        String groupName = chooseGroupNameFromInput(scanner, groups);
        if (groupName == null) { System.out.println("Invalid group selection."); return; }

        Set<UUID> ids = groupService.flattenContactIds(ownerId, groupName);
        if (ids.isEmpty()) { System.out.println("Group is empty."); return; }

        String confirm = prompt(scanner, "Type DELETE GROUP to confirm soft-delete: ");
        if (!"DELETE GROUP".equalsIgnoreCase(confirm.trim())) {
            System.out.println("Cancelled.");
            return;
        }

        BulkOperation op = new BulkSoftDeleteOperation(deletionService);
        BulkResult result = op.apply(ownerId, ids);
        System.out.println(result.summary());
        result.messages().forEach(System.out::println);
    }

    private static void handleBulkExportGroup(Scanner scanner,
                                              AuthService authService,
                                              GroupService groupService,
                                              ContactRepository repository,
                                              ContactRenderer renderer) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        List<String> groups = groupService.listGroupNames(ownerId);
        if (groups.isEmpty()) { System.out.println("No groups."); return; }
        printGroups(groups);

        String groupName = chooseGroupNameFromInput(scanner, groups);
        if (groupName == null) { System.out.println("Invalid group selection."); return; }

        Set<UUID> ids = groupService.flattenContactIds(ownerId, groupName);
        if (ids.isEmpty()) { System.out.println("Group is empty."); return; }

        boolean mask = askYesNo(scanner, "Mask emails in export? (Y/N): ");
        ContactRendererOptions opts = ContactRendererOptions.builder()
                .uppercaseName(false)
                .maskEmails(mask)
                .build();

        BulkExportOperation op = new BulkExportOperation(repository, renderer, opts);
        BulkResult result = op.apply(ownerId, ids);
        System.out.println(result.summary());
        result.messages().forEach(System.out::println);
    }

    // ===== UC-09 =====
    private static void handleSearchContacts(Scanner scanner,
                                             AuthService authService,
                                             SearchService searchService,
                                             ContactRenderer renderer) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        System.out.println("\n=== Search Contacts (leave blank to skip a filter) ===");

        System.out.print("Name contains: ");
        String name = blankToNull(scanner.nextLine());

        System.out.print("Email contains: ");
        String email = blankToNull(scanner.nextLine());

        System.out.print("Phone contains digits (e.g., 202555): ");
        String phoneDigits = blankToNull(scanner.nextLine());

        System.out.print("Type (PERSON/ORGANIZATION, blank = any): ");
        String typeRaw = blankToNull(scanner.nextLine());
        String type = (typeRaw == null) ? null : typeRaw.toUpperCase(Locale.ROOT);

        Specification<Contact> spec = Specification.alwaysTrue();
        if (name != null)  spec = spec.and(ContactSpecifications.nameContainsIgnoreCase(name));
        if (email != null) spec = spec.and(ContactSpecifications.emailContainsIgnoreCase(email));
        if (phoneDigits != null) spec = spec.and(ContactSpecifications.phoneContainsDigits(phoneDigits));
        if ("PERSON".equals(type)) {
            spec = spec.and(ContactSpecifications.typeIsPerson());
        } else if ("ORGANIZATION".equals(type)) {
            spec = spec.and(ContactSpecifications.typeIsOrganization());
        }

        List<Contact> results = searchService.search(ownerId, spec);
        if (results.isEmpty()) {
            System.out.println("No contacts matched your criteria.");
            return;
        }

        System.out.println("\n=== Search Results (" + results.size() + ") ===");
        for (int i = 0; i < results.size(); i++) {
            Contact c = results.get(i);
            System.out.printf("%2d) %s (%s) [id=%s]%n",
                    i + 1,
                    ConsoleContactRenderer.bestEffortName(c),
                    c.getClass().getSimpleName(),
                    c.getId());
        }

        System.out.print("View a result? Enter number or press Enter to skip: ");
        String sel = scanner.nextLine().trim();
        if (!sel.isEmpty()) {
            try {
                int idx = Integer.parseInt(sel);
                if (idx >= 1 && idx <= results.size()) {
                    Contact chosen = results.get(idx - 1);
                    ContactRendererOptions options = ContactRendererOptions.builder()
                            .uppercaseName(false).maskEmails(true).build();
                    System.out.println(renderer.render(chosen, options));
                } else {
                    System.out.println("Invalid selection.");
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid selection.");
            }
        }
    }

    // ===== UC-10 =====
    private static void handleAdvancedFilterSort(Scanner scanner,
                                                 AuthService authService,
                                                 FilterSortService filterSortService,
                                                 ContactRenderer renderer) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        System.out.println("\n=== Advanced Filter & Sort (UC-10) ===");
        List<FilterStrategy<Contact>> filters = new ArrayList<>();

        boolean selecting = true;
        while (selecting) {
            System.out.println("\nAdd filters (choose multiple, 9 to run):");
            System.out.println("1) Has Email");
            System.out.println("2) Has Phone");
            System.out.println("3) Type: Person");
            System.out.println("4) Type: Organization");
            System.out.println("5) Created in last N days");
            System.out.println("6) Updated in last N days");
            System.out.println("7) Email domain contains (e.g., gmail.com)");
            System.out.println("8) Name contains (case-insensitive)");
            System.out.println("9) Done (run)");
            System.out.print("Choose: ");
            String opt = scanner.nextLine().trim();

            switch (opt) {
                case "1" -> { filters.add(new FilterSortService.Filters.HasEmail()); System.out.println("Added filter: Has Email"); }
                case "2" -> { filters.add(new FilterSortService.Filters.HasPhone()); System.out.println("Added filter: Has Phone"); }
                case "3" -> { filters.add(new FilterSortService.Filters.TypePerson()); System.out.println("Added filter: Type Person"); }
                case "4" -> { filters.add(new FilterSortService.Filters.TypeOrganization()); System.out.println("Added filter: Type Organization"); }
                case "5" -> {
                    Integer nd = askPositiveInt(scanner, "Enter N (days): ");
                    if (nd != null) { filters.add(new FilterSortService.Filters.CreatedLastNDays(nd)); System.out.println("Added filter: Created last " + nd + " days"); }
                }
                case "6" -> {
                    Integer nd = askPositiveInt(scanner, "Enter N (days): ");
                    if (nd != null) { filters.add(new FilterSortService.Filters.UpdatedLastNDays(nd)); System.out.println("Added filter: Updated last " + nd + " days"); }
                }
                case "7" -> {
                    System.out.print("Domain contains: ");
                    String d = scanner.nextLine().trim();
                    if (!d.isEmpty()) { filters.add(new FilterSortService.Filters.EmailDomainContains(d)); System.out.println("Added filter: Domain contains '" + d + "'"); }
                }
                case "8" -> {
                    System.out.print("Name contains: ");
                    String n = scanner.nextLine().trim();
                    if (!n.isEmpty()) { filters.add(new FilterSortService.Filters.NameContains(n)); System.out.println("Added filter: Name contains '" + n + "'"); }
                }
                case "9" -> selecting = false;
                default -> { /* ignore invalid; loop */ }
            }
        }

        System.out.println("\nChoose sort order:");
        SortStrategies[] sorts = SortStrategies.values();
        for (int i = 0; i < sorts.length; i++) {
            System.out.printf("%2d) %s%n", i + 1, sorts[i].label());
        }
        System.out.print("Sort choice: ");
        int sIdx = parseIndex(scanner.nextLine().trim(), 1, sorts.length);
        if (sIdx == -1) {
            System.out.println("Invalid sort choice.");
            return;
        }
        SortStrategy<Contact> sortStrategy = sorts[sIdx - 1];

        List<Contact> results = filterSortService.filterAndSort(ownerId, filters, sortStrategy);

        if (results.isEmpty()) {
            System.out.println("No contacts matched the selected filters.");
            return;
        }

        System.out.println("\n=== Filtered & Sorted Results (" + results.size() + ") ===");
        for (int i = 0; i < results.size(); i++) {
            Contact c = results.get(i);
            System.out.printf("%2d) %s (%s) [id=%s]%n",
                    i + 1,
                    ConsoleContactRenderer.bestEffortName(c),
                    c.getClass().getSimpleName(),
                    c.getId());
        }

        System.out.print("View a result? Enter number or press Enter to skip: ");
        String sel = scanner.nextLine().trim();
        if (!sel.isEmpty()) {
            try {
                int idx = Integer.parseInt(sel);
                if (idx >= 1 && idx <= results.size()) {
                    Contact chosen = results.get(idx - 1);
                    ContactRendererOptions options = ContactRendererOptions.builder()
                            .uppercaseName(false).maskEmails(true).build();
                    System.out.println(renderer.render(chosen, options));
                } else {
                    System.out.println("Invalid selection.");
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid selection.");
            }
        }
    }

    // ===== UC-11 =====

    private static void handleCreateTag(Scanner scanner,
                                        AuthService authService,
                                        TagService tagService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }

        System.out.print("New tag name: ");
        String name = scanner.nextLine().trim();
        boolean ok = tagService.createTag(current.get().getId(), name);
        System.out.println(ok ? "Tag created." : "Tag already exists or invalid.");
    }

    private static void handleListTags(Scanner scanner,
                                       AuthService authService,
                                       TagService tagService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }

        List<Tag> tags = tagService.listTags(current.get().getId());
        if (tags.isEmpty()) {
            System.out.println("No tags found.");
            return;
        }
        System.out.println("\n=== Tags ===");
        for (int i = 0; i < tags.size(); i++) {
            System.out.printf("%2d) %s%n", i + 1, tags.get(i).getDisplay());
        }
    }

    private static void handleAssignTagToContact(Scanner scanner,
                                                 AuthService authService,
                                                 ContactService contactService,
                                                 TagService tagService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        List<Contact> contacts = contactService.listMyContacts(ownerId);
        if (contacts.isEmpty()) { System.out.println("You have no contacts."); return; }
        System.out.println("\n=== Select Contact ===");
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%2d) %s (%s) [id=%s]%n", i + 1, ConsoleContactRenderer.bestEffortName(c),
                    c.getClass().getSimpleName(), c.getId());
        }
        System.out.print("Enter number or paste Contact ID: ");
        String sel = scanner.nextLine().trim();
        Optional<Contact> chosen = chooseContactFromInput(contacts, sel);
        if (chosen.isEmpty()) { System.out.println("Invalid selection."); return; }

        List<Tag> tags = tagService.listTags(ownerId);
        if (tags.isEmpty()) {
            System.out.println("No tags available. Create one first.");
            return;
        }
        printTags(tags);
        String tagName = chooseTagNameFromInput(scanner, tags);
        if (tagName == null) { System.out.println("Invalid tag selection."); return; }

        boolean ok = tagService.assignTag(ownerId, chosen.get().getId(), tagName);
        System.out.println(ok ? "Tag assigned to contact." : "Assign failed.");
    }

    private static void handleRemoveTagFromContact(Scanner scanner,
                                                   AuthService authService,
                                                   ContactService contactService,
                                                   TagService tagService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        List<Contact> contacts = contactService.listMyContacts(ownerId);
        if (contacts.isEmpty()) { System.out.println("You have no contacts."); return; }
        System.out.println("\n=== Select Contact ===");
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%2d) %s (%s) [id=%s]%n", i + 1, ConsoleContactRenderer.bestEffortName(c),
                    c.getClass().getSimpleName(), c.getId());
        }
        System.out.print("Enter number or paste Contact ID: ");
        String sel = scanner.nextLine().trim();
        Optional<Contact> chosen = chooseContactFromInput(contacts, sel);
        if (chosen.isEmpty()) { System.out.println("Invalid selection."); return; }

        Set<Tag> tags = tagService.tagsForContact(ownerId, chosen.get().getId());
        if (tags.isEmpty()) {
            System.out.println("This contact has no tags.");
            return;
        }
        List<Tag> asList = new ArrayList<>(tags);
        printTags(asList);
        String tagName = chooseTagNameFromInput(scanner, asList);
        if (tagName == null) { System.out.println("Invalid tag selection."); return; }

        boolean ok = tagService.removeTag(ownerId, chosen.get().getId(), tagName);
        System.out.println(ok ? "Tag removed from contact." : "Remove failed.");
    }

    private static void handleShowContactTags(Scanner scanner,
                                              AuthService authService,
                                              ContactService contactService,
                                              TagService tagService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        List<Contact> contacts = contactService.listMyContacts(ownerId);
        if (contacts.isEmpty()) { System.out.println("You have no contacts."); return; }
        System.out.println("\n=== Select Contact ===");
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%2d) %s (%s) [id=%s]%n", i + 1, ConsoleContactRenderer.bestEffortName(c),
                    c.getClass().getSimpleName(), c.getId());
        }
        System.out.print("Enter number or paste Contact ID: ");
        String sel = scanner.nextLine().trim();
        Optional<Contact> chosen = chooseContactFromInput(contacts, sel);
        if (chosen.isEmpty()) { System.out.println("Invalid selection."); return; }

        Set<Tag> tags = tagService.tagsForContact(ownerId, chosen.get().getId());
        if (tags.isEmpty()) {
            System.out.println("Tags: (none)");
        } else {
            System.out.print("Tags: ");
            boolean first = true;
            for (Tag t : tags) {
                if (!first) System.out.print(", ");
                System.out.print(t.getDisplay());
                first = false;
            }
            System.out.println();
        }
    }

    private static void handleListContactsByTag(Scanner scanner,
                                                AuthService authService,
                                                ContactService contactService,
                                                TagService tagService) {
        Optional<User> current = authService.currentUser();
        if (current.isEmpty()) { System.out.println("Please login first."); return; }
        UUID ownerId = current.get().getId();

        List<Tag> tags = tagService.listTags(ownerId);
        if (tags.isEmpty()) {
            System.out.println("No tags available.");
            return;
        }
        printTags(tags);
        String tagName = chooseTagNameFromInput(scanner, tags);
        if (tagName == null) { System.out.println("Invalid tag selection."); return; }

        Set<UUID> ids = tagService.contactsWithTag(ownerId, tagName);
        if (ids.isEmpty()) {
            System.out.println("No contacts found with tag '" + tagName + "'.");
            return;
        }
        List<Contact> contacts = contactService.listMyContacts(ownerId);
        System.out.println("\n=== Contacts with tag '" + tagName + "' ===");
        int idx = 1;
        for (Contact c : contacts) {
            if (ids.contains(c.getId())) {
                System.out.printf("%2d) %s (%s) [id=%s]%n", idx++,
                        ConsoleContactRenderer.bestEffortName(c),
                        c.getClass().getSimpleName(),
                        c.getId());
            }
        }
        if (idx == 1) {
            System.out.println("(none)");
        }
    }

    // ===== Helpers =====

    private static void printTags(List<Tag> tags) {
        System.out.println("\n=== Tags ===");
        for (int i = 0; i < tags.size(); i++) {
            System.out.printf("%2d) %s%n", i + 1, tags.get(i).getDisplay());
        }
    }

    private static String chooseTagNameFromInput(Scanner scanner, List<Tag> tags) {
        System.out.print("Select tag (number or name): ");
        String sel = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(sel);
            if (idx >= 1 && idx <= tags.size()) {
                return tags.get(idx - 1).getDisplay();
            }
        } catch (NumberFormatException ignored) { }
        for (Tag t : tags) {
            if (t.getDisplay().equalsIgnoreCase(sel)) return t.getDisplay();
        }
        return null;
    }

    private static Integer askPositiveInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        try {
            int n = Integer.parseInt(raw);
            return (n > 0) ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String chooseGroupNameFromInput(Scanner scanner, List<String> groups) {
        System.out.print("Select group (number or name): ");
        String sel = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(sel);
            if (idx >= 1 && idx <= groups.size()) {
                return groups.get(idx - 1);
            }
        } catch (NumberFormatException ignored) { }
        for (String g : groups) {
            if (g.equalsIgnoreCase(sel)) return g;
        }
        return null;
    }

    private static void printGroups(List<String> groups) {
        System.out.println("\n=== Groups ===");
        for (int i = 0; i < groups.size(); i++) {
            System.out.printf("%2d) %s%n", i + 1, groups.get(i));
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
            } catch (NumberFormatException ignored2) { }
        }
        return chosen;
    }

    private static Optional<DeletedContact> chooseDeletedFromInput(List<DeletedContact> list, String sel) {
        Optional<DeletedContact> chosen = Optional.empty();
        try {
            UUID id = UUID.fromString(sel);
            chosen = list.stream().filter(dc -> dc.contact().getId().equals(id)).findFirst();
        } catch (IllegalArgumentException ignored) {
            try {
                int idx = Integer.parseInt(sel);
                if (idx >= 1 && idx <= list.size()) {
                    chosen = Optional.of(list.get(idx - 1));
                }
            } catch (NumberFormatException ignored2) { }
        }
        return chosen;
    }

    private static Optional<UUID> chooseUuidFromInput(List<UUID> list, String sel) {
        try {
            UUID id = UUID.fromString(sel);
            return list.contains(id) ? Optional.of(id) : Optional.empty();
        } catch (IllegalArgumentException ignored) {
            try {
                int idx = Integer.parseInt(sel);
                if (idx >= 1 && idx <= list.size()) {
                    return Optional.of(list.get(idx - 1));
                }
            } catch (NumberFormatException ignored2) { }
        }
        return Optional.empty();
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

    private static String prompt(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private static int parseIndex(String s, int min, int max) {
        try {
            int v = Integer.parseInt(s);
            return (v >= min && v <= max) ? v : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}