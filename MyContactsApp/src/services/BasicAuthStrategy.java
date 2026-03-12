package services;

import domain.User;
import util.Validators;

import java.util.Locale;
import java.util.Optional;

public class BasicAuthStrategy implements AuthenticationStrategy {

    private final UserRepository repository;
    private final PasswordHasher hasher;

    public BasicAuthStrategy(UserRepository repository, PasswordHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
    }

    @Override
    public Optional<User> authenticate(AuthInput input) {
        if (input == null || input.method() != AuthMethod.BASIC) return Optional.empty();
        String email = input.email() == null ? null : input.email().trim().toLowerCase(Locale.ROOT);
        String password = input.password();

        if (!Validators.isValidEmail(email)) return Optional.empty();
        if (password == null) return Optional.empty();

        return repository.findByEmail(email).flatMap(user -> {
            String salt = user.getPasswordSalt();
            String expectedHash = user.getPasswordHash();
            String candidateHash = hasher.hash(password, salt);
            return expectedHash.equals(candidateHash) ? Optional.of(user) : Optional.empty();
        });
    }
}