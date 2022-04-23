package eu.okaeri.commands.service;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Option<T> implements Supplier<Optional<T>> {

    private final T value;

    public static <V> Option<V> of(@Nullable V value) {
        return new Option<>(value);
    }

    public static <V> Option<V> empty() {
        return new Option<>(null);
    }

    public boolean isEmpty() {
        return this.value == null;
    }

    public void ifEmpty(@NonNull Runnable action) {
        if (this.value != null) {
            return;
        }
        action.run();
    }

    public void ifPresent(@NonNull Consumer<? super T> action) {
        if (this.value == null) {
            return;
        }
        action.accept(this.value);
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public void ifPresentOrElse(@NonNull Consumer<? super T> action, @NonNull Runnable emptyAction) {
        if (this.value == null) {
            emptyAction.run();
            return;
        }
        action.accept(this.value);
    }

    public Option<T> filter(@NonNull Predicate<? super T> predicate) {
        if (!this.isPresent()) {
            return this;
        }
        return predicate.test(this.value) ? this : empty();
    }

    public <U> Option<U> map(@NonNull Function<? super T, ? extends U> mapper) {
        if (!this.isPresent()) {
            return empty();
        }
        return Option.of(mapper.apply(this.value));
    }

    @SuppressWarnings("unchecked")
    public <U> Option<U> flatMap(Function<? super T, ? extends Option<? extends U>> mapper) {
        if (!this.isPresent()) {
            return empty();
        }
        Option<U> value = (Option<U>) mapper.apply(this.value);
        return Objects.requireNonNull(value);
    }

    @SuppressWarnings("unchecked")
    public Option<T> or(@NonNull Supplier<? extends Option<? extends T>> supplier) {
        if (this.isPresent()) {
            return this;
        }
        Option<T> value = (Option<T>) supplier.get();
        return Objects.requireNonNull(value);
    }

    public Stream<T> stream() {
        if (this.isPresent()) {
            return Stream.of(this.value);
        }
        return Stream.empty();
    }

    public T orElse(T other) {
        return (this.value == null) ? other : this.value;
    }

    public T orElseNull() {
        return this.value;
    }

    public T orElseGet(@NonNull Supplier<? extends T> supplier) {
        return (this.value == null) ? supplier.get() : this.value;
    }

    public T orElseThrow() {
        if (this.value == null) {
            throw new CommandException("No value present");
        }
        return this.value;
    }

    public <X extends Throwable> T orElseThrow(@NonNull Supplier<? extends X> exceptionSupplier) throws X {
        if (this.value != null) {
            return this.value;
        }
        throw exceptionSupplier.get();
    }

    @Override
    public Optional<T> get() {
        return Optional.ofNullable(this.value);
    }
}
