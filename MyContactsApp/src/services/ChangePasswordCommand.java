package services;

import domain.User;
import util.Validators;

import java.util.Objects;

public class ChangePasswordCommand implements ProfileCommand {

    private final UserRepository repository;
    private final PasswordHasher hasher;
    private final User user;
    private final String currentPassword;
    private final String newPassword;

    private String prevSalt;
    private String prevHash;

    public ChangePasswordCommand(UserRepository repository,
                                 PasswordHasher hasher,
                                 User user,
                                 String currentPassword,
                                 String newPassword) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.hasher = Objects.requireNonNull(hasher, "hasher");
        this.user = Objects.requireNonNull(user, "user");
        this.currentPassword = Objects.requireNonNull(currentPassword, "currentPassword");
        this.newPassword = Objects.requireNonNull(newPassword, "newPassword");
    }

    @Override
    public void execute() {
        if (!Validators.isStrongPassword(newPassword)) {
            throw new IllegalArgumentException("Password must be at least 8 chars, include upper, lower, digit, and special.");
        }

        String salt = user.getPasswordSalt();
        String expected = user.getPasswordHash();
        String candidate = hasher.hash(currentPassword, salt);
        if (!expected.equals(candidate)) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        prevSalt = salt;
        prevHash = expected;

        String newSalt = hasher.generateSalt();
        String newHash = hasher.hash(newPassword, newSalt);
        user.setPasswordMaterial(newSalt, newHash);
        repository.save(user);
    }

    @Override
    public void undo() {
        if (prevSalt != null && prevHash != null) {
            user.setPasswordMaterial(prevSalt, prevHash);
            repository.save(user);
        }
    }

    @Override
    public String description() {
        return "Change Password";
    }
}