package domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class FreeUser extends User {
    private final int maxGroups = 3;

    public FreeUser(UUID id,
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