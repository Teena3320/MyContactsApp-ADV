package services;

import domain.User;
import util.Validators;

import java.util.Objects;

public class UpdateFullNameCommand implements ProfileCommand {

    private final UserRepository repository;
    private final User user;
    private final String newName;
    private String oldName;

    public UpdateFullNameCommand(UserRepository repository, User user, String newName) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.user = Objects.requireNonNull(user, "user");
        this.newName = Objects.requireNonNull(newName, "newName");
    }

    @Override
    public void execute() {
        if (!Validators.isNonBlank(newName)) {
            throw new IllegalArgumentException("Full name cannot be blank.");
        }
        oldName = user.getFullName();
        user.setFullName(newName.trim());
        repository.save(user);
    }

    @Override
    public void undo() {
        if (oldName != null) {
            user.setFullName(oldName);
            repository.save(user);
        }
    }

    @Override
    public String description() {
        return "Update Full Name";
    }
}