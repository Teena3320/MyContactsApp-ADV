package services;

import domain.Contact;

import java.util.*;

public final class BulkExportOperation implements BulkOperation {

    private final ContactRepository repository;
    private final ContactRenderer renderer;
    private final ContactRendererOptions options;

    public BulkExportOperation(ContactRepository repository,
                               ContactRenderer renderer,
                               ContactRendererOptions options) {
        this.repository = Objects.requireNonNull(repository);
        this.renderer = Objects.requireNonNull(renderer);
        this.options = Objects.requireNonNull(options);
    }

    @Override
    public BulkResult apply(UUID ownerId, Set<UUID> contactIds) {
        List<String> outputs = new ArrayList<>();
        int success = 0;
        for (UUID id : contactIds) {
            Optional<Contact> c = repository.findById(id);
            if (c.isPresent() && c.get().getOwnerUserId().equals(ownerId)) {
                String rendered = renderer.render(c.get(), options);
                outputs.add(rendered);
                success++;
            } else {
                outputs.add("Skipped (not found or not owned): " + id);
            }
        }
        int total = contactIds.size();
        int failed = total - success;
        return new BulkResult(total, success, failed, outputs);
    }
}
