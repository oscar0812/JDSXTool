package io.github.oscar0812.JDSX.converters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for operations related to Java files, such as compiling Java code into .class files,
 * converting Java code into Smali code, and extracting class names from Java code.
 */
public class Java {

    /**
     * Compiles a given Java file into .class files and stores them in a specified temporary directory.
     *
     * @param javaFile the path to the Java source file to be compiled
     * @return the directory where the compiled .class files are stored
     * @throws IOException           if an I/O error occurs during compilation or file management
     * @throws IllegalStateException if the Java compiler is not available
     */
    public static Path compileJavaToClass(Path javaFile) throws IOException {
        Path outputDir = FileUtils.getSiblingDirectory(javaFile, "compiled_classes");
        return compileJavaToClass(javaFile, outputDir);
    }

    /**
     * Compiles a given Java file into .class files and stores them in a specified temporary directory.
     *
     * @param javaFile  the path to the Java source file to be compiled
     * @param outputDir the directory to store the compiled .class files
     * @return the directory where the compiled .class files are stored
     * @throws IOException           if an I/O error occurs during compilation or file management
     * @throws IllegalStateException if the Java compiler is not available
     */
    public static Path compileJavaToClass(Path javaFile, Path outputDir) throws IOException {
        FileUtils.validateFilePath(javaFile, "Java path");

        Path classOutputDir = outputDir.resolve("out");
        if (Files.notExists(classOutputDir)) {
            Files.createDirectories(classOutputDir);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                "javac",
                "-d", classOutputDir.toString(),
                javaFile.toString()
        );
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(outputDir.toFile());

        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("javac compilation failed with exit code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Compilation process was interrupted", e);
        }

        if(FileUtils.isDirectoryEmpty(classOutputDir)) {
            throw new IOException("The compilation did not generate files");
        }

        return classOutputDir;
    }

    /**
     * Creates a temporary file containing the given Java code.
     *
     * @param javaCode the Java code to be written to the file
     * @return the path to the created temporary Java file
     * @throws IOException              if an I/O error occurs during file creation or writing
     * @throws IllegalArgumentException if the provided Java code is null or empty
     */
    private static Path createTempJavaFile(String javaCode) throws IOException {
        if (javaCode == null || javaCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Provided Java code is null or empty");
        }

        String className = extractClassName(javaCode);
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("Failed to determine class name from the provided Java code");
        }

        Path tempDir = FileUtils.createTempDirectory("java_temp");
        Path javaFilePath = tempDir.resolve(className + ".java");
        Files.write(javaFilePath, javaCode.getBytes());
        return javaFilePath;
    }

    /**
     * Converts Java code into Smali code
     * Java -> Class -> Dex -> Smali
     *
     * @param javaFilePath the Java file path to be converted
     * @return The diectory path of the generated Smali files
     * @throws Exception if any error occurs during the conversion process
     */
    public static Path convertJavaToSmali(Path javaFilePath) throws Exception {
        Path classOutputDir = compileJavaToClass(javaFilePath);
        Path outputDexPath = Class.convertClassFilesToDex(classOutputDir);
        return Dex.convertDexToSmali(outputDexPath);
    }

    /**
     * Converts Java code into Smali code
     * Java -> Class -> Dex -> Smali
     *
     * @param javaCode the Java code to be converted
     * @return The diectory path of the generated Smali files
     * @throws Exception if any error occurs during the conversion process
     */
    public static Path convertJavaToSmali(String javaCode) throws Exception {
        Path javaFilePath = createTempJavaFile(javaCode);
        return convertJavaToSmali(javaFilePath);
    }


    /**
     * Extracts the class name from a given Java code string using regular expressions.
     *
     * @param javaCode the Java code from which to extract the class name
     * @return the extracted class name, or null if no class name is found
     */
    public static String extractClassName(String javaCode) {
        // Use a regular expression to find the class name
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}