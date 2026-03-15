package services;

public final class ContactRendererOptions {
    private final boolean uppercaseName;
    private final boolean maskEmails;

    private ContactRendererOptions(boolean uppercaseName, boolean maskEmails) {
        this.uppercaseName = uppercaseName;
        this.maskEmails = maskEmails;
    }

    public boolean uppercaseName() { return uppercaseName; }
    public boolean maskEmails() { return maskEmails; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private boolean uppercaseName;
        private boolean maskEmails;

        public Builder uppercaseName(boolean value) { this.uppercaseName = value; return this; }
        public Builder maskEmails(boolean value) { this.maskEmails = value; return this; }
        public ContactRendererOptions build() { return new ContactRendererOptions(uppercaseName, maskEmails); }
    }
}