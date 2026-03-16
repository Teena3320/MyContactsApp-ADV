package domain;

import java.util.Objects;

public final class Tag {
    private final String name;     
    private final String display;   

    public Tag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be blank");
        }
        String norm = name.trim().toLowerCase();
        this.name = norm;
        this.display = name.trim();
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag tag)) return false;
        return name.equals(tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return display;
    }
}