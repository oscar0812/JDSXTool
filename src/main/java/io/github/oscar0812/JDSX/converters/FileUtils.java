package io.github.oscar0812.JDSX.converters;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class providing helper methods for file manipulation and validation
 * used in the context of Java-to-Smali conversion.
 */
public class FileUtils {

    /**
     * Generates a sibling path for the given file path with the specified file extension.
     * The new path is in the same directory as the original file but has a different extension.
     *
     * @param path the original file path
     * @param extension the new file extension (e.g., ".dex", ".smali")
     * @return the generated sibling path with the specified extension
     * @throws IllegalArgumentException if the provided path is invalid or null
     */
    public static Path getSiblingPath(Path path, String extension) {
        String fileNameWithoutExt = path.getFileName().toString().replaceFirst("[.][^.]+$", "");
        return path.toAbsolutePath().getParent().resolve(fileNameWithoutExt + extension);
    }

    /**
     * Generates a sibling directory for the given file path
     * The new path is in the same parent directory as the original path
     *
     * @param path the original file path
     * @param directoryName the directory name
     * @return the generated sibling directory
     * @throws IllegalArgumentException if the provided path is invalid or null
     */
    public static Path getSiblingDirectory(Path path, String directoryName) {
        return path.toAbsolutePath().getParent().resolve(directoryName);
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

    public static boolean isDirectoryEmpty(Path path) throws IOException {
        if (Files.exists(path) && Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                return !stream.iterator().hasNext(); // Returns true if empty
            }
        }
        return false; // Path does not exist or is not a directory
    }

    /**
     * Creates a temporary directory for use in file operations.
     * The directory is automatically scheduled for deletion when the JVM exits.
     *
     * @return the path to the created temporary directory
     * @throws IOException if an I/O error occurs during directory creation
     */
    public static Path createTempDirectory() throws IOException {
        return createTempDirectory("JDSXTool");
    }

    /**
     * Creates a temporary directory with the specified prefix for use in file operations.
     * The directory is automatically scheduled for deletion when the JVM exits.
     *
     * @param dirName the directory name to create
     * @return the path to the created temporary directory
     * @throws IOException if an I/O error occurs during directory creation
     */
    public static Path createTempDirectory(String dirName) throws IOException {
        // Create a temporary directory for all files
        Path tempDir = Files.createTempDirectory(dirName);
        tempDir.toFile().deleteOnExit();  // Ensure it gets deleted when the JVM exits
        return tempDir;
    }

    /**
     * Retrieves all files from the given directory.
     * The method returns an array of all file paths within the directory, without filtering by file extension.
     *
     * @param dirPath the directory to search for files
     * @return a List of paths to all files in the directory
     * @throws IOException if an I/O error occurs while reading the directory
     */
    public static List<Path> findAllFiles(Path dirPath) throws IOException {
        return FileUtils.findFilesByExtension(dirPath, "");
    }

    public static List<Path> findClassFiles(Path directory, String baseName) throws IOException {
        return findAllFiles(directory, path ->
                path.getFileName().toString().matches(baseName + "(?:\\$.*)?\\.class"));
    }

    public static List<Path> findSmaliFiles(Path directory, String baseName) throws IOException {
        return findAllFiles(directory, path ->
                path.getFileName().toString().matches(".*" + baseName + "(?:\\$.*)?\\.smali"));
    }

    public static List<Path> findFilesByExtension(Path directory, String extension) throws IOException {
        return findAllFiles(directory, path -> path.getFileName().toString().endsWith(extension));
    }

    private static List<Path> findAllFiles(Path directory, Predicate<Path> fileFilter) throws IOException {
        List<Path> result;

        try (Stream<Path> stream = Files.walk(directory)) {
            result = stream
                    .filter(Files::isRegularFile).filter(fileFilter)
                    .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Reads the contents of a file located at the specified path and returns it as a single string.
     * The lines of the file are joined with the system's default line separator.
     *
     * @param path the path to the file to read
     * @return the content of the file as a single string
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static String readFileToString(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        return lines.stream()
                .collect(Collectors.joining(System.lineSeparator()));  // Joins lines with the system's line separator
    }

    public static Path copyToTempDir(Path path) throws IOException {
        Path systemTempDir = Paths.get(System.getProperty("java.io.tmpdir"));

        if(path.toAbsolutePath().startsWith(systemTempDir)) {
            // already in systemTempDir
            return path;
        }

        Path newPath = createTempDirectory().resolve(path.getFileName());
        return Files.copy(path.toAbsolutePath(), newPath.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
