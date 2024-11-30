package io.github.oscar0812.JDSX.converters;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
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
     * @param javaFile  the path to the Java source file to be compiled
     * @param outputDir the directory to store the compiled .class files
     * @return the directory where the compiled .class files are stored
     * @throws IOException           if an I/O error occurs during compilation or file management
     * @throws IllegalStateException if the Java compiler is not available
     */
    public static Path compileJavaToClass(Path javaFile, Path outputDir) throws IOException {
        Utils.validateFilePath(javaFile, "Java path");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Java Compiler not available. Ensure you are running with a JDK.");
        }

        // Create a subfolder for the compiled .class files
        Path classOutputDir = outputDir.resolve("compiled_classes");
        if (Files.notExists(classOutputDir)) {
            Files.createDirectories(classOutputDir);
        }

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(javax.tools.StandardLocation.CLASS_OUTPUT, List.of(classOutputDir.toFile()));

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(Collections.singletonList(javaFile));
        boolean success = compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();

        fileManager.close();

        Path[] createdClasses = Utils.getFiles(classOutputDir, ".class");

        if (!success || createdClasses.length == 0) {
            throw new IOException("Failed to compile Java to .class files");
        }

        // Instead of returning the .class files, return the output directory
        return classOutputDir;
    }

    /**
     * Converts Java code into Smali code by first compiling the Java code to .class files,
     * converting the class files into a .dex file, and then converting the .dex file to Smali.
     *
     * @param javaCode the Java code to be converted
     * @return an array of paths to the generated Smali files
     * @throws Exception if any error occurs during the conversion process
     */
    public static Path[] convertJavaToSmali(String javaCode) throws Exception {
        if (javaCode == null || javaCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Provided Java code is null or empty");
        }

        String className = extractClassName(javaCode);
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("Failed to determine class name from the provided Java code");
        }

        Path tempDir = Utils.createTempDirectory();

        Path tempJavaFile = tempDir.resolve(className + ".java");
        Files.write(tempJavaFile, javaCode.getBytes());

        Path classOutputDir = compileJavaToClass(tempJavaFile, tempDir);

        Path outputDexPath = tempDir.resolve("Temp.dex");

        Path[] classFiles = Utils.getFiles(classOutputDir, ".class");

        Class.convertClassFilesToDex(classFiles, outputDexPath);

        Path smaliSubDir = tempDir.resolve("smali");
        Files.createDirectories(smaliSubDir);

        Dex.convertDexToSmali(outputDexPath, smaliSubDir);

        return Utils.getFiles(smaliSubDir, ".smali");
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