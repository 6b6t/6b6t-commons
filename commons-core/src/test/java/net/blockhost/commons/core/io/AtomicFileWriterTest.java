package net.blockhost.commons.core.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AtomicFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void write_bytes_createsFileWithContent() throws Exception {
        Path file = tempDir.resolve("test.bin");
        byte[] content = {1, 2, 3, 4, 5};

        AtomicFileWriter.write(file, content);

        assertArrayEquals(content, Files.readAllBytes(file));
    }

    @Test
    void write_string_createsFileWithUtf8Content() throws Exception {
        Path file = tempDir.resolve("test.txt");

        AtomicFileWriter.write(file, "hello world");

        assertEquals("hello world", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void write_outputStreamConsumer_createsFileWithContent() throws Exception {
        Path file = tempDir.resolve("test.txt");

        AtomicFileWriter.write(file, out -> {
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writer.write("streamed content");
            writer.flush();
        });

        assertEquals("streamed content", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void write_overwritesExistingFile() throws Exception {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "old content");

        AtomicFileWriter.write(file, "new content");

        assertEquals("new content", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void write_createsParentDirectories() throws Exception {
        Path file = tempDir.resolve("nested/deep/dir/test.txt");

        AtomicFileWriter.write(file, "nested content");

        assertTrue(Files.exists(file));
        assertEquals("nested content", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void write_noTempFilesRemainAfterWrite() throws Exception {
        Path file = tempDir.resolve("test.txt");

        AtomicFileWriter.write(file, "content");

        try (Stream<Path> files = Files.list(tempDir)) {
            boolean anyTmp = files.anyMatch(p -> p.getFileName().toString().endsWith(".tmp"));
            assertFalse(anyTmp);
        }
    }

    @Test
    void write_concurrentWritesToSameFile_noCorruption() throws Exception {
        Path file = tempDir.resolve("test.txt");
        int threadCount = 16;

        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                String content = "thread-" + i;
                futures.add(executor.submit(() -> {
                    barrier.await();
                    AtomicFileWriter.write(file, content);
                    return null;
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }
        }

        // The file must contain exactly one complete write from some thread
        String result = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(result.startsWith("thread-"), "File content should be from one thread, got: " + result);

        // No leftover temp files
        try (Stream<Path> files = Files.list(tempDir)) {
            boolean anyTmp = files.anyMatch(p -> p.getFileName().toString().endsWith(".tmp"));
            assertFalse(anyTmp, "Temp files should be cleaned up");
        }
    }

    @Test
    void write_concurrentWritesToDifferentFiles_allSucceed() throws Exception {
        int threadCount = 16;

        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                Path file = tempDir.resolve("file-" + i + ".txt");
                String content = "content-" + i;
                futures.add(executor.submit(() -> {
                    barrier.await();
                    AtomicFileWriter.write(file, content);
                    return null;
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }
        }

        for (int i = 0; i < threadCount; i++) {
            Path file = tempDir.resolve("file-" + i + ".txt");
            assertEquals("content-" + i, Files.readString(file, StandardCharsets.UTF_8));
        }
    }

    @Test
    void write_bytes_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> AtomicFileWriter.write(null, new byte[0]));
    }

    @Test
    void write_bytes_nullContent_throwsNullPointerException() {
        Path file = tempDir.resolve("test.txt");
        assertThrows(NullPointerException.class, () -> AtomicFileWriter.write(file, (byte[]) null));
    }

    @Test
    void write_string_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> AtomicFileWriter.write(null, "content"));
    }

    @Test
    void write_string_nullContent_throwsNullPointerException() {
        Path file = tempDir.resolve("test.txt");
        assertThrows(NullPointerException.class, () -> AtomicFileWriter.write(file, (String) null));
    }

    @Test
    void write_consumer_nullPath_throwsNullPointerException() {
        assertThrows(
                NullPointerException.class, () -> AtomicFileWriter.write(null, _ -> {}));
    }

    @Test
    void write_consumer_nullWriter_throwsNullPointerException() {
        Path file = tempDir.resolve("test.txt");
        assertThrows(NullPointerException.class, () -> AtomicFileWriter.write(file, (IOConsumer<OutputStream>) null));
    }
}
