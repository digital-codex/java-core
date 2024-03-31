package dev.codex.java.io.file;

import dev.codex.java.lang.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class FileTreeWalker {
    private static final String PATH = System.getenv("PATH");

    private static final PathCache CACHE = new PathCache();

    private FileTreeWalker() {
        super();
    }

    public static Result<Path> find(String file) {
        return FileTreeWalker.resolve(FileTreeWalker.PATH, file);
    }

    public static Result<Path> resolve(String directories, String file) {
        if (file == null)
            return Result.failure(
                    new NullPointerException("`file` must be non-null")
            );

        Optional<Path> found = FileTreeWalker.CACHE.get(file);
        if (found.isPresent())
            return Result.success(found.get());

        PathQueue queue = (directories.indexOf(':') < 0)
                ? new PathQueue(new Path[]{ Paths.get(directories) })
                : new PathQueue(
                        Arrays.stream(directories.split(":"))
                                .map(Paths::get)
                                .toArray(Path[]::new)
        );
        PathCache visited = new PathCache();

        while (!queue.isEmpty()) {
            Path current = queue.dequeue();
            if (current == null)
                continue;

            if (Files.isDirectory(current)) {
                if (visited.get(current.toString()).isEmpty()) {
                    try (Stream<Path> stream = Files.list(current)) {
                        stream.forEach(queue::enqueue);
                        visited.set(current.toString(), current);
                    } catch (IOException e) { /* skip */ }
                }
            } else {
                if (FileTreeWalker.ignore(current))
                    continue;

                FileTreeWalker.CACHE.set(
                        FileTreeWalker.filename(current), current
                );
                if (file.compareTo(FileTreeWalker.filename(current)) == 0)
                    return Result.success(current);
            }
        }

        return Result.failure(
                new IllegalArgumentException(
                        "`file` cannot be resolved from `directories`"
                )
        );
    }

    private static String filename(Path path) {
        return path.getFileName().toString();
    }

    private static boolean ignore(Path path) {
        return FileTreeWalker.CACHE.get(FileTreeWalker.filename(path))
                .isPresent();
    }

    private static class PathQueue {
        private Path[] elements;
        private int head;
        private int tail;

        private PathQueue(Path[] paths) {
            this.elements = new Path[16 + 1];
            for (Path path : paths) {
                this.enqueue(path);
            }
        }

        private Path dequeue() {
            Path e = this.elements[this.head];
            if (e != null) {
                this.elements[this.head] = null;
                if (++this.head >= this.elements.length)
                    this.head = 0;
            }
            return e;
        }

        private void enqueue(Path path) {
            this.elements[this.tail] = path;
            if (++tail >= this.elements.length)
                this.tail = 0;

            if (head != tail)
                return;

            final int oldCapacity = this.elements.length;
            final int newCapacity = oldCapacity << 1;
            Path[] newElements = new Path[newCapacity];
            System.arraycopy(
                    this.elements,
                    0,
                    newElements,
                    0,
                    oldCapacity
            );
            this.elements = newElements;

            if (this.elements[this.head] == null)
                return;

            final int newSpace = newCapacity - oldCapacity;
            System.arraycopy(
                    this.elements,
                    this.head,
                    this.elements,
                    this.head + newSpace,
                    oldCapacity - this.head
            );
            this.head += newSpace;
        }

        private boolean isEmpty() {
            return this.head == this.tail;
        }
    }

    private static class PathCache {
        private static class Entry {
            private String key;
            private Path value;

            private Entry(String key, Path value) {
                this.key = key;
                this.value = value;
            }

            private String getKey() {
                return this.key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            private Path getValue() {
                return this.value;
            }

            private void setValue(Path value) {
                this.value = value;
            }
        }

        private static final float LOAD_FACTOR = 0.75f;

        private Entry[] entries;
        private int count;

        private PathCache() {
            this.entries = new Entry[0];
        }

        private Optional<Path> get(String key) {
            return this.isEmpty() ? Optional.empty()
                    : Optional.ofNullable(this.find(key).getValue());
        }

        private void set(String key, Path value) {
            if (this.count + 1 > this.entries.length * PathCache.LOAD_FACTOR) {
                final int newCapacity = this.entries.length < 8 ? 8
                        : this.entries.length << 1;
                Entry[] newEntries = new Entry[newCapacity];
                for (int i = 0; i < newEntries.length; ++i) {
                    newEntries[i] = new Entry(null, null);
                }

                this.count = 0;
                for (Entry entry : this.entries) {
                    if (entry.getKey() == null)
                        continue;

                    int index = (
                            key.hashCode() >= 0 ? key.hashCode()
                                    : -key.hashCode()
                    ) % newEntries.length;
                    for (;;) {
                        Entry dest = newEntries[index];
                        if (dest.getKey() == null && dest.getValue() == null) {
                            dest.setKey(entry.getKey());
                            dest.setValue(entry.getValue());
                            break;
                        }
                        index = ++index % newEntries.length;
                    }
                    this.count++;
                }
                this.entries = newEntries;
            }
            Entry entry = this.find(key);
            if (entry.getKey() == null && entry.getValue() == null)
                this.count++;

            entry.setKey(key);
            entry.setValue(value);
        }

        private Entry find(String key) {
            int index = (
                    key.hashCode() >= 0 ? key.hashCode() : -key.hashCode()
            ) % this.entries.length;
            for (;;) {
                Entry entry = this.entries[index];
                if (entry.getKey() == null && entry.getValue() == null) {
                    return entry;
                } else if (Objects.equals(key, entry.getKey()))
                    return entry;

                index = ++index % this.entries.length;
            }
        }

        private boolean isEmpty() {
            return this.count == 0;
        }
    }
}