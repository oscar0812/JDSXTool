package io.github.oscar0812.JDSX.converters;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Java {

    public static File[] compileJavaToClass(File javaFile, File tempDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Java Compiler not available. Ensure you are running with a JDK.");
        }

        // Create a subfolder for the compiled .class files
        File classOutputDir = new File(tempDir, "compiled_classes");
        if (!classOutputDir.exists() && !classOutputDir.mkdirs()) {
            throw new IOException("Failed to create directory for compiled classes");
        }

        // Set up the file manager to use the subfolder
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(javax.tools.StandardLocation.CLASS_OUTPUT, List.of(classOutputDir));

        // Compile the Java file
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(javaFile));
        boolean success = compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();

        fileManager.close();

        if (!success) {
            throw new IOException("Failed to compile Java to .class files");
        }

        // Retrieve all .class files in the subfolder
        File[] classFiles = classOutputDir.listFiles((dir, name) -> name.endsWith(".class"));
        if (classFiles == null || classFiles.length == 0) {
            throw new IOException("No .class files were generated");
        }

        return classFiles;
    }

    public static Path[] convertJavaToSmali(String javaCode) throws Exception {
        String className = extractClassName(javaCode);
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("Failed to determine class name from the provided Java code");
        }

        Path tempDir = Utils.createTempDirectory();

        Path tempJavaFile = tempDir.resolve(className + ".java");
        Files.write(tempJavaFile, javaCode.getBytes());

        File[] classFiles = compileJavaToClass(tempJavaFile.toFile(), tempDir.toFile());

        String[] classFilePaths = new String[classFiles.length];
        for (int i = 0; i < classFiles.length; i++) {
            classFilePaths[i] = classFiles[i].toString();
        }

        String outputDexPath = tempDir.resolve("Temp.dex").toString();
        Dex.convertClassFilesToDex(classFilePaths, outputDexPath);

        Path smaliSubDir = tempDir.resolve("smali");
        Files.createDirectories(smaliSubDir);

        Dex.convertDexToSmali(outputDexPath, smaliSubDir.toString());

        return Utils.getFiles(smaliSubDir, "smali");
    }

    private static String extractClassName(String javaCode) {
        // Use a regular expression to find the class name
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
