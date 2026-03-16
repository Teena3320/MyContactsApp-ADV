package services;

import domain.Tag;

import java.time.LocalDateTime;
import java.util.UUID;

public record TagEvent(
        UUID ownerId,
        UUID contactId,
        Tag tag,
        TagEventType type,
        LocalDateTime at
) { }