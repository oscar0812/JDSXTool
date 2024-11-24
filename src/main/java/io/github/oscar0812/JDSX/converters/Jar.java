package io.github.oscar0812.JDSX.converters;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

public class Jar {

    public static Path extractJar(Path jarPath) {
        Utils.validateFilePath(jarPath, "JAR path");
        Path outputDir = Utils.generateSiblingPath(jarPath, "_extract");
        try {
            Files.createDirectories(outputDir);
            extractJar(jarPath, outputDir);
        } catch (IOException e) {
            throw new RuntimeException("Error extracting classes from JAR", e);
        }
        return outputDir;
    }

    public static void extractJar(Path jarPath, Path destinationDir) throws IOException {
        Utils.validateFilePath(jarPath, "JAR path");
        if (destinationDir == null) {
            throw new IllegalArgumentException("Destination directory cannot be null.");
        }
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            jarFile.stream().forEach(entry -> {
                Path entryPath = destinationDir.resolve(entry.getName());
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

    public static Path convertClassJarToJavaJar(Path jarPath) {
        Utils.validateFilePath(jarPath, "JAR path");
        Path outputDir = Utils.generateSiblingPath(jarPath, "_java");
        try {
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
        } catch (IOException e) {
            throw new RuntimeException("Error decompiling JAR to Java", e);
        }
        return outputDir;
    }
}
