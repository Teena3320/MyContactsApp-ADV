package services;

public interface ProfileCommand {
    void execute();
    void undo();
    String description();
}