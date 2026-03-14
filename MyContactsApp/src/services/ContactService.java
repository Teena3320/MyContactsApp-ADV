package services;

import domain.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ContactService {

    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public PersonContact createPerson(UUID ownerUserId,
                                      String firstName,
                                      String lastName,
                                      List<PhoneNumber> phones,
                                      List<EmailAddress> emails) {

        ContactFactory.PersonBuilder builder = ContactFactory.person(ownerUserId)
                .firstName(firstName)
                .lastName(lastName);

        if (phones != null) {
            for (PhoneNumber p : phones) {
                builder.addPhone(p.getType(), p.getNumber());
            }
        }
        if (emails != null) {
            for (EmailAddress e : emails) {
                builder.addEmail(e.getType(), e.getAddress());
            }
        }

        PersonContact contact = builder.build();
        repository.save(contact);
        return contact;
    }

    public OrganizationContact createOrganization(UUID ownerUserId,
                                                  String organizationName,
                                                  List<PhoneNumber> phones,
                                                  List<EmailAddress> emails) {
        ContactFactory.OrganizationBuilder builder = ContactFactory.organization(ownerUserId)
                .organizationName(organizationName);

        if (phones != null) {
            for (PhoneNumber p : phones) {
                builder.addPhone(p.getType(), p.getNumber());
            }
        }
        if (emails != null) {
            for (EmailAddress e : emails) {
                builder.addEmail(e.getType(), e.getAddress());
            }
        }

        OrganizationContact contact = builder.build();
        repository.save(contact);
        return contact;
    }

    public List<Contact> listMyContacts(UUID ownerUserId) {
        return repository.findAllByOwner(ownerUserId);
    }
}