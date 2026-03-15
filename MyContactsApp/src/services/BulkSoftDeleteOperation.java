package services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BulkSoftDeleteOperation implements BulkOperation {

    private final ContactDeletionService deletionService;

    public BulkSoftDeleteOperation(ContactDeletionService deletionService) {
        this.deletionService = deletionService;
    }

    @Override
    public BulkResult apply(UUID ownerId, Set<UUID> contactIds) {
        int total = contactIds.size();
        int success = 0;
        List<String> messages = new ArrayList<>();
        for (UUID id : contactIds) {
            boolean ok = deletionService.softDelete(ownerId, id);
            if (ok) {
                success++;
                messages.add("Soft-deleted: " + id);
            } else {
                messages.add("Failed to soft-delete: " + id);
            }
        }
        int failed = total - success;
        return new BulkResult(total, success, failed, messages);
    }
}