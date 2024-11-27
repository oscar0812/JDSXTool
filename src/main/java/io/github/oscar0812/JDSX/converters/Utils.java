package io.github.oscar0812.JDSX.converters;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class providing helper methods for file manipulation and validation
 * used in the context of Java-to-Smali conversion.
 */
class Utils {

    /**
     * Generates a sibling path for the given file path with the specified file extension.
     * The new path is in the same directory as the original file but has a different extension.
     *
     * @param path the original file path
     * @param extension the new file extension (e.g., ".dex", ".smali")
     * @return the generated sibling path with the specified extension
     */
    public static Path generateSiblingPath(Path path, String extension) {
        String fileNameWithoutExt = path.getFileName().toString().replaceFirst("[.][^.]+$", "");
        return path.toAbsolutePath().getParent().resolve(fileNameWithoutExt + extension);
    }

    /**
     * Validates the given file path by checking if the path is not null and if the file exists.
     * Throws an exception if the path is null or if the file does not exist.
     *
     * @param path the path to be validated
     * @param description a description of the file (used in exception messages)
     * @throws IOException if the file does not exist
     * @throws IllegalArgumentException if the path is null
     */
    public static void validateFilePath(Path path, String description) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException(description + " cannot be null.");
        }
        if (!Files.exists(path)) {
            throw new IOException(description + " does not exist: " + path);
        }
    }

    /**
     * Creates a temporary directory with the prefix "JavaToSmali" for use in file operations.
     * The directory is automatically scheduled for deletion when the JVM exits.
     *
     * @return the path to the created temporary directory
     * @throws IOException if an I/O error occurs during directory creation
     */
    public static Path createTempDirectory() throws IOException {
        // Create a temporary directory for all files
        Path tempDir = Files.createTempDirectory("JavaToSmali");
        tempDir.toFile().deleteOnExit();  // Ensure it gets deleted when the JVM exits
        return tempDir;
    }

    /**
     * Retrieves all files with the specified extension from the given directory.
     *
     * @param smaliDir the directory to search for files
     * @param extension the file extension to filter by (e.g., "smali")
     * @return an array of paths to the files with the specified extension
     * @throws IOException if an I/O error occurs while reading the directory
     */
    public static Path[] getFiles(Path smaliDir, String extension) throws IOException {
        List<Path> smaliFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(smaliDir, "*." + extension)) {
            for (Path entry : stream) {
                smaliFiles.add(entry);
            }
        }
        return smaliFiles.toArray(new Path[0]);
    }
}