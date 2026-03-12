package services;

import domain.FreeUser;
import domain.PremiumUser;
import domain.User;
import domain.UserType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class UserFactory {

    public static Builder builder(UserType type, PasswordHasher hasher) {
        return new Builder(type, hasher);
    }

    public static final class Builder {
        private final UserType type;
        private final PasswordHasher hasher;

        private String fullName;
        private String email;
        private String rawPassword;

        private Builder(UserType type, PasswordHasher hasher) {
            this.type = Objects.requireNonNull(type, "type");
            this.hasher = Objects.requireNonNull(hasher, "hasher");
        }

        public Builder fullName(String name) {
            this.fullName = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String rawPassword) {
            this.rawPassword = rawPassword;
            return this;
        }

        public User build() {
            Objects.requireNonNull(fullName, "fullName");
            Objects.requireNonNull(email, "email");
            Objects.requireNonNull(rawPassword, "rawPassword");

            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.now();
            String salt = hasher.generateSalt();
            String hash = hasher.hash(rawPassword, salt);

            return switch (type) {
                case FREE -> new FreeUser(id, email, fullName, salt, hash, createdAt);
                case PREMIUM -> new PremiumUser(id, email, fullName, salt, hash, createdAt);
            };
        }
    }
}