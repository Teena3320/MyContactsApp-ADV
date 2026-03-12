package util;

import java.util.regex.Pattern;

public final class Validators {

    private Validators() {}

    private static final Pattern EMAIL_RX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PW_UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern PW_LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern PW_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern PW_SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    public static boolean isNonBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_RX.matcher(email).matches();
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        if (!PW_UPPER.matcher(password).matches()) return false;
        if (!PW_LOWER.matcher(password).matches()) return false;
        if (!PW_DIGIT.matcher(password).matches()) return false;
        if (!PW_SPECIAL.matcher(password).matches()) return false;
        return true;
    }
}