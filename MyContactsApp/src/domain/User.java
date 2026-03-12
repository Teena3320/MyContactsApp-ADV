package domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class User {
    private final UUID id;
    private final String email;              // immutable identifier
    private String fullName;                 // mutable profile field
    private String passwordSalt;             // private sensitive
    private String passwordHash;             // private sensitive
    private final LocalDateTime createdAt;

    protected User(UUID id,
                   String email,
                   String fullName,
                   String passwordSalt,
                   String passwordHash,
                   LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.email = Objects.requireNonNull(email, "email");
        this.fullName = Objects.requireNonNull(fullName, "fullName");
        this.passwordSalt = Objects.requireNonNull(passwordSalt, "passwordSalt");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String newName) {
        this.fullName = Objects.requireNonNull(newName, "newName");
    }

    public void setPasswordMaterial(String salt, String hash) {
        this.passwordSalt = Objects.requireNonNull(salt, "salt");
        this.passwordHash = Objects.requireNonNull(hash, "hash");
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}