package dev.codex.java.io.file;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileTreeWalkerTest {
    @Test
    void test_find_cmake_executable_then_assert() {
        Path result = FileTreeWalker.find("cmake").orElse(null);
        assertNotNull(result);
        assertEquals("/usr/local/bin/cmake", result.toString());
        result = FileTreeWalker.find("ninja").orElse(null);
        assertNotNull(result);
        assertEquals("/usr/bin/ninja", result.toString());
        result = FileTreeWalker.find("clang").orElse(null);
        assertNotNull(result);
        assertEquals("/usr/bin/clang", result.toString());
        result = FileTreeWalker.find("clang++").orElse(null);
        assertNotNull(result);
        assertEquals("/usr/bin/clang++", result.toString());
    }

    @Test
    void test_resolve_cmake_executable_then_assert() {
        Path result = FileTreeWalker.resolve(
                "/usr/local/bin", "cmake"
        ).orElse(null);
        assertNotNull(result);
        assertEquals("/usr/local/bin/cmake", result.toString());
    }
}