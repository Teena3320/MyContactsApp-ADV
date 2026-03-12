package services;

import domain.User;

import java.util.Optional;

public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private volatile User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public synchronized void login(User user) {
        this.currentUser = user;
    }

    public synchronized void logout() {
        this.currentUser = null;
    }

    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }
}