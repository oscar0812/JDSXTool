package io.github.oscar0812.JDSX.converters;

import com.android.dx.command.dexer.DxContext;
import com.android.dx.command.dexer.Main;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Class {
    /**
     * Converts an array of `.class` files into a single `.dex` file.
     * <p>
     * This method takes an array of `.class` file paths and converts them into a single `.dex` file
     * using the specified output path. The input paths must point to valid `.class` files.
     * </p>
     *
     * @param inputClassFilePaths an array of paths to the input `.class` files
     * @param outputDexPath       the path to the output `.dex` file
     * @return the path to the generated `.dex` file
     * @throws IllegalArgumentException if {@code inputClassFilePaths} is null, empty, or contains invalid paths,
     *                                  or if {@code outputDexPath} is null
     * @throws IOException              if an error occurs while accessing the file system
     * @throws RuntimeException         if an error occurs during the conversion process
     */
    public static Path convertClassFilesToDex(Path[] inputClassFilePaths, Path outputDexPath) throws IOException {
        if (inputClassFilePaths == null || inputClassFilePaths.length == 0) {
            throw new IllegalArgumentException("No input class files provided");
        }

        if (outputDexPath == null) {
            throw new IllegalArgumentException("Output DEX file path cannot be null or empty");
        }

        for (Path classFilePath : inputClassFilePaths) {
            if (classFilePath == null || !Files.exists(classFilePath) || !Files.isRegularFile(classFilePath)) {
                throw new IllegalArgumentException("Invalid input class file path: " + classFilePath);
            }
        }

        DxContext dxContext = new DxContext();
        Main.Arguments arguments = new Main.Arguments();
        arguments.outName = outputDexPath.toString();
        arguments.strictNameCheck = false;

        // Convert Path[] to String[] for compatibility with the dx tool
        arguments.fileNames = java.util.Arrays.stream(inputClassFilePaths)
                .map(Path::toString)
                .toArray(String[]::new);

        new Main(dxContext).runDx(arguments);

        return outputDexPath;
    }

    /**
     * Converts `.class` files to a `.dex` file from a single input path.
     * <p>
     * If the input path is a directory, all `.class` files within the directory (including subdirectories) are converted.
     * If the input path is a single `.class` file, only that file is converted.
     * </p>
     *
     * @param inputPath     the path to a directory containing `.class` files or a single `.class` file
     * @param outputDexPath the path to the output `.dex` file
     * @return the path to the generated `.dex` file
     * @throws IllegalArgumentException if the input path is invalid, doesn't contain `.class` files,
     *                                  or if the output path is null
     * @throws IOException              if an error occurs while accessing the file system
     */
    public static Path convertClassFilesToDex(Path inputPath, Path outputDexPath) throws IOException {
        if (inputPath == null || !Files.exists(inputPath)) {
            throw new IllegalArgumentException("Input path is invalid or does not exist: " + inputPath);
        }

        if (outputDexPath == null) {
            throw new IllegalArgumentException("Output DEX file path cannot be null or empty");
        }

        List<Path> classFiles = new ArrayList<>();

        if (Files.isDirectory(inputPath)) {
            // Fetch all `.class` files in the directory
            try (var stream = Files.walk(inputPath)) {
                stream.filter(Files::isRegularFile)
                        .filter(file -> file.toString().endsWith(".class"))
                        .forEach(classFiles::add);
            }
        } else if (Files.isRegularFile(inputPath) && inputPath.toString().endsWith(".class")) {
            // Single `.class` file
            classFiles.add(inputPath);
        } else {
            throw new IllegalArgumentException("Input path is not a `.class` file or a directory containing `.class` files: " + inputPath);
        }

        if (classFiles.isEmpty()) {
            throw new IllegalArgumentException("No `.class` files found in the input path: " + inputPath);
        }

        return convertClassFilesToDex(classFiles.toArray(new Path[0]), outputDexPath);
    }

    /**
     * Converts `.class` files to a `.dex` file using an auto-generated sibling output path.
     * <p>
     * The `.dex` file is created as a sibling to the inputPath
     * </p>
     *
     * @param inputPath the path to a directory containing `.class` files or a single `.class` file
     * @return the path to the generated `.dex` file
     * @throws IllegalArgumentException if the input path is invalid or doesn't contain `.class` files
     * @throws IOException              if an error occurs while accessing the file system
     */
    public static Path convertClassFilesToDex(Path inputPath) throws IOException {
        Path dexPath = Utils.generateSiblingPath(inputPath, ".dex");
        return convertClassFilesToDex(inputPath, dexPath);
    }
}
