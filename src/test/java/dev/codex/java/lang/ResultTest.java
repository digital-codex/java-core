package dev.codex.java.lang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    @Test
    void test_if_success_then_assert() {
        Result.success("hello world").ifSuccess(x -> assertEquals("hello world", x));
    }

    @Test
    void test_if_failure_then_assert() {
        Result.failure(new RuntimeException()).ifSuccess(x -> fail());
    }

    @Test
    void test_if_success_or_else_of_success_then_assert() {
        Result.success("hello world").ifSuccessOrElse(x -> assertEquals("hello world", x), Assertions::fail);
    }

    @Test
    void test_if_success_or_else_of_failure_then_assert() {
        Result.failure(new RuntimeException()).ifSuccessOrElse(x -> fail(), () -> assertTrue(true));
    }

    @Test
    void test_get_result_of_success_then_assert() {
        Result<String> result = Result.success("hello world");
        assertEquals("hello world", result.get());
    }

    @Test
    void test_get_result_of_failure_then_assert() {
        Result<String> result = Result.failure(new RuntimeException());
        assertThrows(NoSuchElementException.class, result::get);
    }

    @Test
    void test_get_with_message_result_of_success_then_assert() {
        Result<String> result = Result.success("hello world");
        assertEquals(
                "hello world",
                result.get("An exception shouldn't be thrown")
        );
    }

    @Test
    void test_get_with_message_result_of_failure_then_assert() {
        Result<String> result = Result.failure(new RuntimeException());
        assertThrows(
                NoSuchElementException.class,
                () -> result.get("An exception should be thrown")
        );
    }

    @Test
    void test_or_else_result_of_success_then_assert() {
        Result<String> result = Result.success("hello world");
        assertEquals(
                "hello world",
                result.orElse("hello world")
        );
    }

    @Test
    void test_or_else_result_of_failure_then_assert() {
        Result<String> result = Result.failure(new RuntimeException());
        assertEquals(
                "goodbye moon",
                result.orElse("goodbye moon")
        );
    }

    @Test
    void test_or_else_get_result_of_success_then_assert() {
        Result<String> result = Result.success("hello world");
        assertEquals(
                "hello world",
                result.orElseGet(() -> "hello world")
        );
    }

    @Test
    void test_or_else_get_result_of_failure_then_assert() {
        Result<String> result = Result.failure(new RuntimeException());
        assertEquals(
                "goodbye moon",
                result.orElseGet(() -> "goodbye moon")
        );
    }
}