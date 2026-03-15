package services;

import domain.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SearchService {

    private final ContactRepository repository;

    public SearchService(ContactRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<Contact> search(UUID ownerId, Specification<Contact> spec) {
        List<Contact> base = repository.findAllByOwner(ownerId);
        if (base.isEmpty()) return List.of();
        List<Contact> out = new ArrayList<>();
        for (Contact c : base) {
            if (spec.isSatisfiedBy(c)) out.add(c);
        }
        return out;
    }
}