package eu.okaeri.commands.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Option<T> {

    public static <V> Option<V> of(V value) {
        return new Option<>(value);
    }

    public boolean isNull() {
        return this.value == null;
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public Optional<T> get() {
        return Optional.ofNullable(this.value);
    }

    public T getOrNull() {
        return this.value;
    }

    private final T value;
}
