package domain;

import java.util.Objects;

public final class PhoneNumber {
    private final PhoneType type;
    private final String number;

    public PhoneNumber(PhoneType type, String number) {
        this.type = Objects.requireNonNull(type, "type");
        this.number = Objects.requireNonNull(number, "number");
    }

    public PhoneType getType() { return type; }
    public String getNumber() { return number; }

    @Override
    public String toString() {
        return type + ":" + number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber that)) return false;
        return type == that.type && number.equals(that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }
}