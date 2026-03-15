package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BulkResult {
    private final int total;
    private final int success;
    private final int failed;
    private final List<String> messages;

    public BulkResult(int total, int success, int failed, List<String> messages) {
        this.total = total;
        this.success = success;
        this.failed = failed;
        this.messages = new ArrayList<>(messages);
    }

    public int total() { return total; }
    public int success() { return success; }
    public int failed() { return failed; }
    public List<String> messages() { return Collections.unmodifiableList(messages); }

    public String summary() {
        return String.format("BulkResult: total=%d, success=%d, failed=%d", total, success, failed);
    }
}