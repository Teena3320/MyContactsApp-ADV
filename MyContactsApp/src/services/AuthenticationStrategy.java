package services;

import domain.User;

import java.util.Optional;

public interface AuthenticationStrategy {
    Optional<User> authenticate(AuthInput input);
}