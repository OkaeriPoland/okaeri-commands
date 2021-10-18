package eu.okaeri.commands.service;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Option<T> implements Supplier<Optional<T>> {

    public static <V> Option<V> of(@Nullable V value) {
        return new Option<>(value);
    }

    public boolean isNull() {
        return this.value == null;
    }

    public boolean isPresent() {
        return this.value != null;
    }

    @Override
    public Optional<T> get() {
        return Optional.ofNullable(this.value);
    }

    @Nullable
    public T getOrNull() {
        return this.value;
    }

    public T getOr(@NonNull T element) {
        return (this.value == null) ? element : this.value;
    }

    private final T value;
}
