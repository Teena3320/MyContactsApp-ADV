package domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class PremiumUser extends User {
    private final int maxGroups = 50;

    public PremiumUser(UUID id,
                       String email,
                       String fullName,
                       String passwordSalt,
                       String passwordHash,
                       LocalDateTime createdAt) {
        super(id, email, fullName, passwordSalt, passwordHash, createdAt);
    }

    public int getMaxGroups() {
        return maxGroups;
    }
}