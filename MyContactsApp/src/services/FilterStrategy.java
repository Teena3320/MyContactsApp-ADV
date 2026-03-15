package services;

@FunctionalInterface
public interface FilterStrategy<T> {
    boolean test(T t);
}