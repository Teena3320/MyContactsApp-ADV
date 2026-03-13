package services;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandHistory {
    private final Deque<ProfileCommand> undoStack = new ArrayDeque<>();
    private final Deque<ProfileCommand> redoStack = new ArrayDeque<>();

    public void record(ProfileCommand cmd) {
        undoStack.push(cmd);
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public String undo() {
        if (!canUndo()) return "Nothing to undo.";
        ProfileCommand cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        return "Undone: " + cmd.description();
    }

    public String redo() {
        if (!canRedo()) return "Nothing to redo.";
        ProfileCommand cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        return "Redone: " + cmd.description();
    }
}