package services;

import java.util.ArrayDeque;
import java.util.Deque;

public class ContactCommandHistory {

    private final Deque<ContactEditCommand> undoStack = new ArrayDeque<>();
    private final Deque<ContactEditCommand> redoStack = new ArrayDeque<>();

    public String recordAndExecute(ContactEditCommand cmd) {
        String msg = cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
        return msg;
    }

    public String undo() {
        if (undoStack.isEmpty()) return "Nothing to undo.";
        ContactEditCommand cmd = undoStack.pop();
        String msg = cmd.undo();
        redoStack.push(cmd);
        return msg;
    }

    public String redo() {
        if (redoStack.isEmpty()) return "Nothing to redo.";
        ContactEditCommand cmd = redoStack.pop();
        String msg = cmd.redo();
        undoStack.push(cmd);
        return msg;
    }
}