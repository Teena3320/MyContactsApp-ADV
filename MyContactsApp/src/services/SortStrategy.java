package services;

import java.util.Comparator;

public interface SortStrategy<T> {
    String label();
    Comparator<T> comparator();
}