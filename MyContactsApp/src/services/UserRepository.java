package services;

import domain.User;

import java.util.Optional;

public interface UserRepository {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    void save(User user);
}