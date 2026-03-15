package services;

import java.util.Set;
import java.util.UUID;

public interface GroupMember {
    Set<UUID> flattenContactIds();
}