package io.github.oscar0812.JDSX.converters;

import com.googlecode.d2j.smali.BaksmaliCmd;
import com.googlecode.dex2jar.tools.Dex2jarCmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for converting between DEX, JAR, and Smali formats.
 * Provides methods for validating files and converting between these formats.
 */
public class Dex {
    /**
     * Converts a DEX file to a JAR file. The output JAR file is created in the same directory
     * as the input DEX file, with the same base name and a `.jar` extension.
     *
     * @param dexPath the path to the input DEX file
     * @return the path to the generated JAR file
     * @throws IllegalArgumentException if {@code dexPath} is invalid or the file is not a valid DEX file
     * @throws IOException              if an error occurs while accessing the file system
     */
    public static Path convertDexToClassJar(Path dexPath) throws IOException {
        Utils.validateFilePath(dexPath, "Dex path");

        Path jarPath = Utils.getSiblingPath(dexPath, ".jar");
        return convertDexToClassJar(dexPath, jarPath);
    }

    /**
     * Converts a DEX file to a JAR file.
     *
     * @param dexPath the path to the input DEX file
     * @param jarPath the path to the output JAR file
     * @return the path to the generated JAR file
     * @throws IllegalArgumentException if {@code dexPath} is invalid or {@code jarPath} is null
     * @throws RuntimeException         if an error occurs during the conversion
     * @throws IOException              if an error occurs while accessing the file system
     */
    public static Path convertDexToClassJar(Path dexPath, Path jarPath) throws IOException {
        Utils.validateFilePath(dexPath, "Dex path");

        if (jarPath == null) {
            throw new IllegalArgumentException("JAR output path cannot be null.");
        }

        if (!isValidDexFile(dexPath)) {
            throw new IllegalArgumentException("The provided file is not a valid DEX file: " + dexPath);
        }

        Dex2jarCmd.main(dexPath.toString(), "-o", jarPath.toString(), "--force");

        return jarPath;
    }

    /**
     * Converts a DEX file to Smali files, creating a sibling folder for the output.
     * The sibling folder is named based on the base name of the input DEX file.
     *
     * @param dexFilePath the path to the input DEX file
     * @return the path to the folder containing the generated Smali files
     * @throws IllegalArgumentException if {@code dexFilePath} is invalid
     * @throws RuntimeException         if an error occurs during the conversion
     * @throws IOException              if an error occurs while accessing the file system
     */
    public static Path convertDexToSmali(Path dexFilePath) throws IOException {
        Utils.validateFilePath(dexFilePath, "Dex path");

        // Create a sibling directory for the Smali files
        Path outputDir = Utils.getSiblingPath(dexFilePath, "_smali");
        Files.createDirectories(outputDir);

        return convertDexToSmali(dexFilePath, outputDir);
    }

    /**
     * Converts a DEX file to Smali files.
     *
     * @param dexFilePath the path to the input DEX file
     * @param outputDir   the directory where the Smali files will be written
     * @return the path to the directory containing the generated Smali files
     * @throws IllegalArgumentException if {@code dexFilePath} or {@code outputDir} is invalid
     * @throws RuntimeException         if an error occurs during the conversion
     * @throws IOException              if an error occurs while accessing the file system
     */
    public static Path convertDexToSmali(Path dexFilePath, Path outputDir) throws IOException {
        Utils.validateFilePath(dexFilePath, "Dex path");

        if (outputDir == null) {
            throw new IllegalArgumentException("Output directory path cannot be null or empty");
        }

        String[] args = {dexFilePath.toString(), "-o", outputDir.toString(), "--force"};
        try {
            BaksmaliCmd.main(args);
        } catch (Exception e) {
            throw new RuntimeException("Error converting DEX to Smali", e);
        }
        return outputDir;
    }

    /**
     * Converts a DEX file to Java source code.
     * Dex -> Class Jar -> Java
     *
     * @param dexFilePath the path to the input DEX file
     * @return the path to the generated Java source code file
     * @throws IOException if an error occurs during the conversion process
     */
    public static Path convertDexToJava(Path dexFilePath) throws IOException {
        Path classJar = Dex.convertDexToClassJar(dexFilePath);
        return Jar.convertClassJarToJava(classJar);
    }

    /**
     * Checks if the given file is a valid DEX file by reading its magic header.
     * <p>
     * This method reads the first 8 bytes of the file and checks if it matches one of the valid
     * DEX file magic headers ("dex\n035\0" or "dex\n036\0").
     * </p>
     *
     * @param filePath the path to the file to check
     * @return {@code true} if the file is a valid DEX file, {@code false} otherwise
     * @throws IllegalArgumentException if {@code filePath} is null or invalid
     */
    public static boolean isValidDexFile(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }

        try {
            // Read the first 8 bytes of the file
            byte[] header = new byte[8];
            try (var inputStream = Files.newInputStream(filePath)) {
                if (inputStream.read(header) != 8) {
                    return false; // File is too small to be a valid DEX file
                }
            }

            // DEX magic header is "dex\n035\0" or "dex\n036\0"
            String headerString = new String(header, 0, 8);
            return "dex\n035\0".equals(headerString) || "dex\n036\0".equals(headerString);
        } catch (IOException e) {
            return false;
        }
    }
}