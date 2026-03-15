package services;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface Specification<T> {

    boolean isSatisfiedBy(T t);

    default Specification<T> and(Specification<T> other) {
        Objects.requireNonNull(other);
        return t -> this.isSatisfiedBy(t) && other.isSatisfiedBy(t);
    }

    default Specification<T> or(Specification<T> other) {
        Objects.requireNonNull(other);
        return t -> this.isSatisfiedBy(t) || other.isSatisfiedBy(t);
    }

    default Specification<T> not() {
        return t -> !this.isSatisfiedBy(t);
    }

    static <T> Specification<T> from(Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        return predicate::test;
    }

    static <T> Specification<T> alwaysTrue() {
        return t -> true;
    }
}