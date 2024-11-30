package io.github.oscar0812.JDSX.converters;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
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
        Path outputDir = Utils.getSiblingPath(jarPath, "_extract");
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
     * Converts a JAR file containing compiled `.class` files into a JAR file containing Java source files.
     *
     * @param jarPath the path to the JAR file containing compiled `.class` files
     * @return the path to the decompiled JAR containing `.java` source files
     * @throws IllegalArgumentException if the provided JAR file does not contain `.class` files
     * @throws IOException if an I/O error occurs during decompiling, file extraction, or JAR creation
     */
    public static Path convertClassJarToJavaJar(Path jarPath) throws IOException {
        Utils.validateFilePath(jarPath, "JAR path");

        if (!isClassJar(jarPath)) {
            throw new IllegalArgumentException("The provided JAR file does not contain `.class` files.");
        }

        Path outputDir = Utils.getSiblingPath(jarPath, "_java_jar");
        Files.createDirectories(outputDir);

        // Decompile the class files to Java source
        ConsoleDecompiler.main(new String[]{
                jarPath.toString(),
                outputDir.toString()
        });

        // Find the decompiled JAR and return its path
        return Files.list(outputDir)
                .filter(path -> path.toString().endsWith(".jar"))
                .findFirst()
                .orElseThrow(() -> new IOException("Decompiled JAR not found."));
    }

    /**
     * Converts a JAR file containing compiled `.class` files into a directory of Java source files.
     *
     * @param jarPath the path to the JAR file containing compiled class files
     * @return the path to the directory containing the decompiled Java source files
     * @throws IOException if an I/O error occurs during decompiling or file extraction
     */
    public static Path convertClassJarToJava(Path jarPath) throws IOException {
        Path decompiledJar = Jar.convertClassJarToJavaJar(jarPath);
        Path outputDir = Utils.getSiblingPath(jarPath, "_java");
        Jar.extractJar(decompiledJar, outputDir);
        return outputDir;
    }

    /**
     * Converts a JAR file containing compiled `.class` files into a `.dex` file.
     * The `.class` files are first extracted from the JAR and then converted to `.dex`.
     *
     * @param jarPath the path to the JAR file containing compiled `.class` files
     * @return the path to the generated `.dex` file
     * @throws IOException if an I/O error occurs during extraction or conversion
     */
    public static Path convertClassJarToDex(Path jarPath) throws IOException {
        Path classDir = Jar.extractJar(jarPath);
        return Class.convertClassFilesToDex(classDir);
    }

    /**
     * Converts a JAR file containing compiled `.class` files into a `.smali` file.
     * The `.class` files are first extracted from the JAR and then converted to `.smali`.
     *
     * @param jarPath the path to the JAR file containing compiled `.class` files
     * @return the path to the generated `.smali` file
     * @throws IOException if an I/O error occurs during extraction or conversion
     */
    public static Path convertClassJarToSmali(Path jarPath) throws IOException {
        Path classDir = Jar.extractJar(jarPath);
        return Class.convertClassFilesToSmali(classDir);
    }

    /**
     * Checks if a JAR file contains `.class` files.
     *
     * @param jarPath the path to the JAR file
     * @return true if the JAR contains `.class` files, false otherwise
     * @throws IOException if an I/O error occurs while reading the JAR file
     */
    public static boolean isClassJar(Path jarPath) throws IOException {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    return true; // It's a class JAR
                }
            }
        }
        return false; // Not a class JAR
    }
}
