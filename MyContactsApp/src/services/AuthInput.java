package services;

import java.util.Objects;

public final class AuthInput {
    private final AuthMethod method;
    private final String email;
    private final String password;

    private AuthInput(AuthMethod method, String email, String password) {
        this.method = Objects.requireNonNull(method, "method");
        this.email = email;
        this.password = password;
    }

    public static AuthInput basic(String email, String password) {
        return new AuthInput(AuthMethod.BASIC, email, password);
    }

    public AuthMethod method() { return method; }
    public String email() { return email; }
    public String password() { return password; }
}