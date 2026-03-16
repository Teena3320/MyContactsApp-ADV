# UC‑01: User Registration

Input: full name, email, password, type (FREE/PREMIUM).

Early validation and password hashing.

Service: RegistrationService

Repository: UserRepository (in‑memory)

# UC‑02: User Authentication (Login)

Input: email & password.

Strategy‑driven authentication (BASIC strategy implemented).

Service: AuthService (+ SessionManager as Singleton)

# UC‑03: User Profile Management

Update profile name, change password (current‑password required).

Command Pattern for undo/redo of profile changes.

Services: UpdateFullNameCommand, ChangePasswordCommand, CommandHistory

# UC‑04: Create Contact

Create PersonContact or OrganizationContact with multiple phones/emails.

Domain validation, timestamps, UUIDs.

Service: ContactService

# UC‑05: View Contact Details

List and view formatted details (type/name/phones/emails/timestamps).

Optional display flags: uppercase name, mask emails.

Renderer abstraction: ContactRenderer (ConsoleContactRenderer)

# UC‑06: Edit Contact

Update names, replace phone numbers/emails.

Command Pattern + Memento‑style history for undo/redo of edits.

Services: ContactEditService, edit commands, ContactCommandHistory

# UC‑07: Delete Contact

Soft delete (Trash with restore/purge) and hard delete.

Service: ContactDeletionService, DeletedContact

# UC‑08: Contact Groups (Bulk Ops)

Create groups, add/remove members, list, bulk soft‑delete, and export.

Composite Pattern (ContactGroup, ContactLeaf) for uniform member handling.

Service: GroupService, bulk operations

# UC‑09: Search Contacts

Specification Pattern for combinable criteria: name/email/phone/type.

Case‑insensitive string matches, phone digit normalization.

Service: SearchService, ContactSpecifications, Specification<T>

# UC‑10: Advanced Filter & Sort

Pluggable FilterStrategy and SortStrategy (name, created/updated, type→name).

Time‑based filters (created/updated in last N days), email domain, name contains.

Service: FilterSortService, SortStrategies enum

# UC‑11: Create & Manage Tags

Owner‑scoped tags with normalized equality (Flyweight‑style).

Assign/de‑assign and list tags; contacts maintain consistent tag sets.

Domain: Tag (value object)

Service: TagService

# UC‑12: Apply Tags to Contacts + Observe Changes

Assign/remove tags to contacts with Observer notifications.

Real‑time console logging via ConsoleTagObserver.

Service: TagService + TagObserver, TagEvent, TagEventType
