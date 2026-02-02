package net.blockhost.commons.core.io;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/// Utility class for writing files atomically using a write-to-temp-then-rename strategy.
///
/// This prevents partial or corrupt files if the process crashes mid-write. The sequence is:
///
/// 1. Write content to a temporary file in the same directory as the target
/// 2. Atomically move the temporary file to the target path
/// 3. If atomic move is not supported by the filesystem, fall back to a regular move
///
/// The temporary file is always cleaned up, even if an error occurs during writing.
///
/// ## Usage
///
/// ```java
/// // Write a string
/// AtomicFileWriter.write(path, "key: value\n");
///
/// // Write bytes
/// AtomicFileWriter.write(path, serializedBytes);
///
/// // Write via an OutputStream (e.g. for YAML serializers)
/// AtomicFileWriter.write(path, out -> yaml.dump(data, new OutputStreamWriter(out)));
/// ```
@UtilityClass
public class AtomicFileWriter {

    /// Writes the given byte array to the target path atomically.
    ///
    /// @param path the target file path
    /// @param content the bytes to write
    /// @throws IOException if an I/O error occurs
    /// @throws NullPointerException if path or content is null
    public void write(Path path, byte[] content) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(content, "content");

        write(path, out -> out.write(content));
    }

    /// Writes the given string to the target path atomically using UTF-8 encoding.
    ///
    /// @param path the target file path
    /// @param content the string to write
    /// @throws IOException if an I/O error occurs
    /// @throws NullPointerException if path or content is null
    public void write(Path path, String content) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(content, "content");

        write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    /// Writes to the target path atomically using a consumer that receives an [OutputStream].
    ///
    /// This overload is useful when the caller wants to serialize directly to a stream
    /// (e.g. YAML or JSON writers) without first buffering the entire content into a byte array.
    ///
    /// @param path the target file path
    /// @param writer a consumer that writes content to the provided output stream
    /// @throws IOException if an I/O error occurs
    /// @throws NullPointerException if path or writer is null
    public void write(Path path, IOConsumer<OutputStream> writer) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(writer, "writer");

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        long suffix = ThreadLocalRandom.current().nextLong() & Long.MAX_VALUE;
        Path tempFile = path.resolveSibling("." + path.getFileName() + "." + suffix + ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(tempFile)) {
                writer.accept(out);
            }
            atomicMove(tempFile, path);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /// Moves the source file to the target, attempting an atomic move first.
    ///
    /// If the filesystem does not support atomic moves, falls back to a regular
    /// move with [StandardCopyOption#REPLACE_EXISTING].
    private void atomicMove(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException _) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
