package domain;

import java.util.Objects;

public final class EmailAddress {
    private final EmailType type;
    private final String address;

    public EmailAddress(EmailType type, String address) {
        this.type = Objects.requireNonNull(type, "type");
        this.address = Objects.requireNonNull(address, "address");
    }

    public EmailType getType() { return type; }
    public String getAddress() { return address; }

    @Override
    public String toString() {
        return type + ":" + address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAddress that)) return false;
        return type == that.type && address.equalsIgnoreCase(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, address.toLowerCase());
    }
}