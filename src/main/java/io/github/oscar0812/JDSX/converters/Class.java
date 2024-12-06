package io.github.oscar0812.JDSX.converters;

import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.OutputMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Class {

    /**
     * Converts `.class` files to a `.dex` file using an auto-generated sibling output path.
     * Uses android R8/D8
     * Class -> Dex
     *
     * @param inputPath the path to a directory containing `.class` files or a single `.class` file
     * @return the path to the generated `.dex` file
     * @throws IllegalArgumentException if the input path is invalid or doesn't contain `.class` files
     * @throws IOException              if an error occurs while accessing the file system
     */
    public static Path convertClassFilesToDex(Path inputPath) throws IOException {
        inputPath = FileUtils.moveToTempDirIfNeeded(inputPath);

        Path[] paths;
        if (Files.isRegularFile(inputPath)) {
            paths = new Path[]{inputPath};
        } else {
            paths = FileUtils.getFiles(inputPath, ".class");
        }

        Path dexDir = Files.createDirectories(FileUtils.getSiblingDirectory(inputPath, "dex_out"));

        try {
            D8Command command = D8Command.builder()
                    .addProgramFiles(paths)
                    .setOutput(dexDir, OutputMode.DexIndexed)
                    .build();

            D8.run(command);
        } catch (CompilationFailedException e) {
            throw new RuntimeException(e);
        }

        Path[] outputDexPaths = FileUtils.getFiles(dexDir, ".dex");
        if (outputDexPaths.length == 0) {
            throw new IOException("Dex was not generated");
        }

        return outputDexPaths[0];
    }

    /**
     * Converts `.class` files to Smali code.
     * Class -> Dex -> Smali
     *
     * @param inputPath the path to a directory containing `.class` files or a single `.class` file
     * @return the path to the generated Smali file
     * @throws IOException if an error occurs during the conversion process
     */
    public static Path convertClassFilesToSmali(Path inputPath) throws IOException {
        Path dexPath = convertClassFilesToDex(inputPath);
        return Dex.convertDexToSmali(dexPath);
    }

    /**
     * Converts `.class` files to a `.class.jar` file.
     * Class -> Dex -> ClassJar
     *
     * @param inputPath the path to a directory containing `.class` files or a single `.class` file
     * @return the path to the generated `.class.jar` file
     * @throws IOException if an error occurs during the conversion process
     */
    public static Path convertClassFilesToClassJar(Path inputPath) throws IOException {
        Path dexPath = convertClassFilesToDex(inputPath);
        return Dex.convertDexToClassJar(dexPath);
    }

    /**
     * Converts `.class` files to Java code.
     * Class -> Dex -> ClassJar -> Java
     *
     * @param inputPath the path to a directory containing `.class` files or a single `.class` file
     * @return the path to the generated Java file
     * @throws IOException if an error occurs during the conversion process
     */
    public static Path convertClassFilesToJava(Path inputPath) throws IOException {
        Path dexPath = convertClassFilesToDex(inputPath);
        Path classJar = Dex.convertDexToClassJar(dexPath);
        return Jar.convertClassJarToJava(classJar);
    }
}