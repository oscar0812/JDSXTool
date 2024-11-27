package io.github.oscar0812.JDSX.converters;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

/**
 * Utility class for operations related to JAR files, such as extracting contents,
 * decompiling Java classes to source code, and converting JARs to Java JARs.
 */
public class Jar {

    /**
     * Extracts the contents of a JAR file to a sibling directory.
     *
     * @param jarPath the path to the JAR file to extract
     * @return the path to the directory containing the extracted contents
     * @throws IOException if an I/O error occurs during extraction
     */
    public static Path extractJar(Path jarPath) throws IOException {
        Utils.validateFilePath(jarPath, "JAR path");
        Path outputDir = Utils.generateSiblingPath(jarPath, "_extract");
        Files.createDirectories(outputDir);
        extractJar(jarPath, outputDir);

        return outputDir;
    }

    /**
     * Extracts the contents of a JAR file to a specified destination directory.
     *
     * @param jarPath the path to the JAR file to extract
     * @param destinationDir the directory to extract the contents into
     * @throws IOException if an I/O error occurs during extraction
     * @throws IllegalArgumentException if the destination directory is null
     * @throws SecurityException if a JAR entry is found with a relative path outside the extraction directory
     */
    public static void extractJar(Path jarPath, Path destinationDir) throws IOException {
        Utils.validateFilePath(jarPath, "JAR path");
        if (destinationDir == null) {
            throw new IllegalArgumentException("Destination directory cannot be null.");
        }
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            jarFile.stream().forEach(entry -> {
                Path entryPath = destinationDir.resolve(entry.getName());

                if (entry.getName().startsWith("..")) {
                    throw new SecurityException("Invalid JAR entry with relative path outside the extraction directory: " + entry.getName());
                }

                try {
                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        Files.copy(jarFile.getInputStream(entry), entryPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("Error extracting: " + entry.getName(), e);
                }
            });
        }
    }

    /**
     * Converts a JAR file containing compiled classes into a JAR file containing Java source code,
     * by decompiling the class files into Java source files.
     *
     * @param jarPath the path to the JAR file containing compiled classes
     * @return the path to the directory containing the decompiled Java files
     * @throws IOException if an I/O error occurs during decompiling or extraction
     */
    public static Path convertClassJarToJavaJar(Path jarPath) throws IOException {
        Utils.validateFilePath(jarPath, "JAR path");
        Path outputDir = Utils.generateSiblingPath(jarPath, "_java");
        Files.createDirectories(outputDir);
        ConsoleDecompiler.main(new String[]{
                jarPath.toString(),
                outputDir.toString()
        });
        Path decompiledJar = Files.list(outputDir)
                .filter(path -> path.toString().endsWith(".jar"))
                .findFirst()
                .orElseThrow(() -> new IOException("Decompiled JAR not found."));
        Jar.extractJar(decompiledJar, outputDir);
        Files.deleteIfExists(decompiledJar);
        return outputDir;
    }
}
