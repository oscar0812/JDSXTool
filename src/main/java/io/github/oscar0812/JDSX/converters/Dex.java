package io.github.oscar0812.JDSX.converters;

import com.android.dx.command.dexer.DxContext;
import com.android.dx.command.dexer.Main;
import com.googlecode.d2j.smali.BaksmaliCmd;
import com.googlecode.dex2jar.tools.Dex2jarCmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for converting between DEX, JAR, and Smali formats.
 */
public class Dex {
    /**
     * Converts a DEX file to a JAR file. The output JAR file will be created in the same directory
     * as the input DEX file with the same base name and a `.jar` extension.
     *
     * @param dexPath the path to the input DEX file
     * @return the path to the generated JAR file
     * @throws IllegalArgumentException if {@code dexPath} is invalid or the file is not a valid DEX file
     * @throws RuntimeException         if an error occurs during the conversion
     */
    public static Path convertDexToJar(Path dexPath) {
        validateFilePath(dexPath, "Dex path");

        // Determine the sibling JAR file path
        Path jarPath = Utils.generateSiblingPath(dexPath, ".jar");

        // Call the existing method
        return convertDexToJar(dexPath, jarPath);
    }

    /**
     * Converts a DEX file to a JAR file.
     *
     * @param dexPath the path to the input DEX file
     * @param jarPath the path to the output JAR file
     * @return the path to the generated JAR file
     * @throws IllegalArgumentException if {@code dexPath} is invalid or {@code jarPath} is null
     * @throws RuntimeException         if an error occurs during the conversion
     */
    public static Path convertDexToJar(Path dexPath, Path jarPath) {
        validateFilePath(dexPath, "Dex path");

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
     * Converts an array of class files into a DEX file.
     *
     * @param inputClassFilePaths an array of paths to the input class files
     * @param outputDexPath       the path to the output DEX file
     * @throws IOException      if an error occurs during file processing
     * @throws RuntimeException if an error occurs during the conversion
     */
    public static void convertClassFilesToDex(String[] inputClassFilePaths, Path outputDexPath) throws IOException {
        if (inputClassFilePaths == null || inputClassFilePaths.length == 0) {
            throw new IllegalArgumentException("No input class files provided");
        }

        if (outputDexPath == null) {
            throw new IllegalArgumentException("Output DEX file path cannot be null or empty");
        }

        DxContext dxContext = new DxContext();
        Main.Arguments arguments = new Main.Arguments();
        arguments.outName = outputDexPath.toString();
        arguments.strictNameCheck = false;
        arguments.fileNames = inputClassFilePaths;

        new Main(dxContext).runDx(arguments);
    }

    /**
     * Converts a DEX file to Smali files.
     *
     * @param dexFilePath the path to the input DEX file
     * @param outputDir   the directory where the Smali files will be written
     * @throws RuntimeException if an error occurs during the conversion
     */
    public static void convertDexToSmali(Path dexFilePath, Path outputDir) {
        validateFilePath(dexFilePath, "Dex path");

        if (outputDir == null) {
            throw new IllegalArgumentException("Output directory path cannot be null or empty");
        }

        String[] args = {dexFilePath.toString(), "-o", outputDir.toString(), "--force"};
        try {
            BaksmaliCmd.main(args);
        } catch (Exception e) {
            throw new RuntimeException("Error converting DEX to Smali", e);
        }
    }

    /**
     * Checks if the given file is a valid DEX file by reading its magic header.
     *
     * @param filePath the path to the file to check
     * @return true if the file is a valid DEX file, false otherwise
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
            return headerString.equals("dex\n035\0") || headerString.equals("dex\n036\0");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Validates if a given file path is valid and the file exists.
     *
     * @param filePath  the path to validate
     * @param paramName the name of the parameter (for error reporting)
     * @throws IllegalArgumentException if the file path is invalid
     */
    public static void validateFilePath(Path filePath, String paramName) {
        if (filePath == null) {
            throw new IllegalArgumentException(paramName + " cannot be null.");
        }

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException(paramName + " does not exist: " + filePath);
        }

        if (!Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException(paramName + " is not a regular file: " + filePath);
        }

        if (!Files.isReadable(filePath)) {
            throw new IllegalArgumentException(paramName + " is not readable: " + filePath);
        }
    }
}
