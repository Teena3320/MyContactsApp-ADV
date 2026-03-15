package services;

import java.util.UUID;

public interface SupportsHardDelete {
    boolean deleteById(UUID id);
}