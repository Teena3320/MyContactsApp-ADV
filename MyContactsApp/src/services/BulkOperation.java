package services;

import java.util.Set;
import java.util.UUID;

public interface BulkOperation {
    BulkResult apply(UUID ownerId, Set<UUID> contactIds);
}