package dev.codex.java.lang;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

// TODO: add equals, hashCode, and toString
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
}