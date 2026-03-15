package services;

import domain.Contact;

public interface ContactRenderer {
    String render(Contact contact, ContactRendererOptions options);
}
