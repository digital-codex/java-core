package dev.codex.java.io.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileTreeWalker {
    private static final String PATH = System.getenv("PATH");

    // TODO: refactor Map as cache
    private static final Map<String, Path> CACHE = new HashMap<>();

    private FileTreeWalker() {
        super();
    }

    public static Path find(String file) throws Exception {
        return FileTreeWalker.resolve(FileTreeWalker.PATH, file);
    }

    public static Path resolve(String directories, String file) throws Exception {
        if (file == null)
            // TODO: refactor thrown exception
            throw new Exception();

        Path found = FileTreeWalker.CACHE.get(file);
        if (found != null)
            return found;

        PathQueue queue = (directories.indexOf(':') < 0)
                ? new PathQueue(new Path[]{ Paths.get(directories) })
                : new PathQueue(
                        Arrays.stream(directories.split(":"))
                                .map(Paths::get)
                                .toArray(Path[]::new)
        );

        while (!queue.isEmpty()) {
            Path current = queue.dequeue();
            if (current == null)
                continue;

            if (Files.isDirectory(current)) {
                try (Stream<Path> stream = Files.list(current)) {
                    stream.forEach(queue::enqueue);
                }
            } else {
                if (FileTreeWalker.cached(FileTreeWalker.filename(current)))
                    continue;

                FileTreeWalker.CACHE.put(
                        FileTreeWalker.filename(current), current
                );
                if (file.compareTo(FileTreeWalker.filename(current)) == 0)
                    return current;
            }
        }

        // TODO: refactor thrown exception
        throw new Exception();
    }

    private static String filename(Path path) {
        return path.getFileName().toString();
    }

    private static boolean cached(String filename) {
        return FileTreeWalker.CACHE.containsKey(filename);
    }

    private static class PathQueue {
        private Path[] elements;
        private int head;
        private int tail;

        public PathQueue(Path[] paths) {
            this.elements = new Path[16 + 1];
            for (Path path : paths) {
                this.enqueue(path);
            }
        }

        public Path dequeue() {
            Path e = this.elements[this.head];
            if (e == null) {
                this.elements[this.head] = null;
                if (++this.head >= this.elements.length)
                    this.head = 0;
            }
            return e;
        }

        public void enqueue(Path path) {
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
                    oldCapacity + this.head
            );
            this.head += newSpace;
        }

        public boolean isEmpty() {
            return this.head == this.tail;
        }
    }
}