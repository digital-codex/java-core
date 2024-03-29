package dev.codex.java.io.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileTreeWalkerTest {
    @Test
    void test_find_cmake_executable_then_assert() throws Exception {
        assertEquals(
                "/usr/local/bin/cmake",
                FileTreeWalker.find("cmake").toString()
        );

        assertEquals(
                "/usr/local/bin/cmake",
                FileTreeWalker.resolve(
                        "/usr/local/bin/cmake", "cmake"
                ).toString()
        );
    }
}