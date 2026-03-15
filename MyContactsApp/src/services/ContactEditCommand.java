package services;

import java.util.UUID;

public interface ContactEditCommand {
    UUID contactId();
    String execute();
    String undo();
    String redo();
}