package services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public class PasswordHasher {

    private static final int SALT_BYTES = 16;
    private final SecureRandom random = new SecureRandom();

    public String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    public String hash(String rawPassword, String hexSalt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = HexFormat.of().parseHex(hexSalt);
            digest.update(saltBytes);
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }
}