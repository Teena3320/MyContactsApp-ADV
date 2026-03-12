package services;

import domain.User;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AuthService {

    private final Map<AuthMethod, AuthenticationStrategy> strategies;
    private final SessionManager sessionManager;

    public AuthService(Map<AuthMethod, AuthenticationStrategy> strategies,
                       SessionManager sessionManager) {
        this.strategies = Objects.requireNonNull(strategies, "strategies");
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager");
    }

    public Optional<User> login(AuthInput input) {
        if (input == null) return Optional.empty();
        AuthenticationStrategy strategy = strategies.get(input.method());
        if (strategy == null) return Optional.empty();

        Optional<User> user = strategy.authenticate(input);
        user.ifPresent(sessionManager::login);
        return user;
    }

    public void logout() {
        sessionManager.logout();
    }

    public Optional<User> currentUser() {
        return sessionManager.currentUser();
    }
}
