package services;

import domain.Contact;

import java.time.LocalDateTime;

public record DeletedContact(Contact contact, LocalDateTime deletedAt) { }