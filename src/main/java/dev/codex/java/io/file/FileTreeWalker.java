package dev.codex.java.io.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileTreeWalker {
    private static final String PATH = System.getenv("PATH");

    // TODO: refactor Map as cache
    private static final Map<String, Path> CACHE = new HashMap<>();

    private static Path[] queue;
    private static int head;
    private static int tail;

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

        FileTreeWalker.queue = new Path[16];
        FileTreeWalker.head = 0;
        FileTreeWalker.tail = 0;

        if (directories.indexOf(':') < 0) {
            FileTreeWalker.queue[tail++] = Paths.get(directories);
        } else {
            for (String path : directories.split(":")) {
                FileTreeWalker.enqueue(Paths.get(path));
            }
        }

        while (FileTreeWalker.head != FileTreeWalker.tail) {
            Path current = FileTreeWalker.queue[FileTreeWalker.head];
            if (current == null)
                continue;

            FileTreeWalker.queue[FileTreeWalker.head] = null;
            if (++FileTreeWalker.head >= FileTreeWalker.queue.length)
                FileTreeWalker.head = 0;

            if (Files.isDirectory(current)) {
                try (Stream<Path> stream = Files.list(current)) {
                    for (Path path : stream.toArray(Path[]::new)) {
                        FileTreeWalker.enqueue(path);
                    }
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

    private static void enqueue(Path path) {
        FileTreeWalker.queue[FileTreeWalker.tail] = path;
        if (++FileTreeWalker.tail >= FileTreeWalker.queue.length)
            FileTreeWalker.tail = 0;

        if (FileTreeWalker.head != FileTreeWalker.tail)
            return;

        final int oldCapacity = FileTreeWalker.queue.length;
        final int newCapacity = oldCapacity << 1;
        Path[] newQueue = new Path[newCapacity];
        final int newSpace = newCapacity - oldCapacity;
        System.arraycopy(
                FileTreeWalker.queue,
                FileTreeWalker.head,
                newQueue,
                FileTreeWalker.head + newSpace,
                oldCapacity - FileTreeWalker.head
        );
        FileTreeWalker.queue = newQueue;
        FileTreeWalker.head += newSpace;
    }
}