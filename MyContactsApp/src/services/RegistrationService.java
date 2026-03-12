package services;

import domain.User;
import domain.UserType;
import util.ValidationException;
import util.Validators;

import java.util.Locale;

public class RegistrationService {

    private final UserRepository repository;
    private final PasswordHasher hasher;

    public RegistrationService(UserRepository repository, PasswordHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
    }

    public User register(String fullName, String email, String rawPassword, UserType type) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase(Locale.ROOT);

        if (!Validators.isNonBlank(fullName)) {
            throw new ValidationException("Full name cannot be blank.");
        }
        if (!Validators.isValidEmail(normalizedEmail)) {
            throw new ValidationException("Invalid email format.");
        }
        if (!Validators.isStrongPassword(rawPassword)) {
            throw new ValidationException("Password must be at least 8 chars, include upper, lower, digit, and special.");
        }
        if (repository.existsByEmail(normalizedEmail)) {
            throw new ValidationException("An account with this email already exists.");
        }

        User user = UserFactory
                .builder(type, hasher)
                .fullName(fullName.trim())
                .email(normalizedEmail)
                .password(rawPassword)
                .build();

        repository.save(user);
        return user;
    }
}