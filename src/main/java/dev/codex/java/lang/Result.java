package dev.codex.java.lang;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Result<T> {
    private final T success;
    private final Exception failure;

    private Result(T success, Exception failure) {
        this.success = success;
        this.failure = failure;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> failure(Exception exception) {
        return new Result<>(null, exception);
    }

    public boolean isSuccess() {
        return this.success != null;
    }

    public boolean isFailure() {
        return this.failure != null;
    }

    public void ifSuccess(Consumer<? super T> consumer) {
        if (this.isSuccess()) {
            consumer.accept(this.success);
        }
    }

    public void ifSuccessOrElse(Consumer<? super T> consumer, Runnable runnable) {
        if (this.isSuccess()) {
            consumer.accept(this.success);
        } else {
            runnable.run();
        }
    }

    public T get() {
        if (this.isFailure())
            throw new NoSuchElementException(this.failure);

        return this.success;
    }

    public T get(String message) {
        if (this.isFailure())
            throw new NoSuchElementException(message, this.failure);

        return this.success;
    }

    public T orElse(T other) {
        return this.isSuccess() ? this.success : other;
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return this.isSuccess() ? this.success : supplier.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Result<?> that)) return false;
        return Objects.equals(this.success, that.success)
                && Objects.equals(this.failure, that.failure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.success, this.failure);
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + this.success.toString()
                + ", failure=" + this.failure.toString()
                + "}";
    }
}