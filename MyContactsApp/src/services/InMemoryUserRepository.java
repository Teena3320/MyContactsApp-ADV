package services;

import domain.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> byEmail = new ConcurrentHashMap<>();

    @Override
    public boolean existsByEmail(String email) {
        return byEmail.containsKey(email.toLowerCase());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(byEmail.get(email.toLowerCase()));
    }

    @Override
    public void save(User user) {
        byEmail.put(user.getEmail().toLowerCase(), user);
    }
}