package io.github.oscar0812.JDSX.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class JavaTest {

    private Path javaFile;
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("javaTest");
        javaFile = tempDir.resolve("TestClass.java");
        String javaCode = "public class TestClass {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}";
        Files.write(javaFile, javaCode.getBytes());
    }

    @Test
    public void testCompileJavaToClass_Success() throws IOException {
        Path classesDir = Java.compileJavaToClass(javaFile, tempDir);
        assertNotNull(classesDir);

        Path[] classFiles = Utils.getFiles(classesDir);
        assertTrue(classFiles.length > 0);
        assertTrue(classFiles[0].toString().endsWith(".class"));
    }

    @Test
    public void testCompileJavaToClass_FileDoesNotExist() {
        Path nonExistentFile = tempDir.resolve("NonExistentClass.java");
        assertThrows(IOException.class, () -> {
            Java.compileJavaToClass(nonExistentFile, tempDir);
        });
    }

    @Test
    public void testConvertJavaToSmali_Success() throws Exception {
        String javaCode = "public class TestClass {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}";
        Path[] smaliFiles = Java.convertJavaToSmali(javaCode);
        assertNotNull(smaliFiles);
        assertTrue(smaliFiles.length > 0);
        assertTrue(smaliFiles[0].toString().endsWith(".smali"));
    }

    @Test
    public void testConvertJavaToSmali_EmptyJavaCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            Java.convertJavaToSmali("");
        });
    }

    @Test
    public void testConvertJavaToSmali_InvalidClassName() {
        String javaCode = "public class { public static void main(String[] args) {} }";
        assertThrows(IllegalArgumentException.class, () -> {
            Java.convertJavaToSmali(javaCode);
        });
    }

    @Test
    public void testExtractClassName_Success() {
        String javaCode = "public class TestClass {\n" +
                "    public static void main(String[] args) { }\n" +
                "}";
        String className = Java.extractClassName(javaCode);
        assertNotNull(className);
        assertEquals("TestClass", className);
    }

    @Test
    public void testExtractClassName_NoClass() {
        String javaCode = "public void main(String[] args) {}";
        String className = Java.extractClassName(javaCode);
        assertNull(className);
    }

    @Test
    public void testConvertJavaToSmali_InvalidJavaCode() {
        String invalidJavaCode = "public class InvalidClass {\n" +
                "    public static void main(String args { }\n" +
                "}";
        assertThrows(Exception.class, () -> {
            Java.convertJavaToSmali(invalidJavaCode);
        });
    }

    @Test
    public void testCompileJavaToClass_EmptyJavaFile() throws IOException {
        Path emptyJavaFile = tempDir.resolve("EmptyClass.java");
        Files.write(emptyJavaFile, "".getBytes());
        assertThrows(IOException.class, () -> {
            Java.compileJavaToClass(emptyJavaFile, tempDir);
        });
    }

    @Test
    public void testCompileJavaToClass_FolderExists() throws IOException {
        String javaCode = "public class FolderExists {\n" +
                "    public static void main(String[] args) {}\n" +
                "}";
        javaFile = tempDir.resolve("FolderExists.java");
        Files.write(javaFile, javaCode.getBytes());
        Path classesDir = Java.compileJavaToClass(javaFile, tempDir);
        assertNotNull(classesDir);

        Path[] classFiles = Utils.getFiles(classesDir);
        assertTrue(classFiles.length > 0);
        assertTrue(classFiles[0].toString().endsWith(".class"));
    }

    @Test
    public void testCompileJavaToClass_InvalidJavaCode() throws IOException {
        Path invalidJavaFile = tempDir.resolve("InvalidSyntax.java");
        String invalidCode = "public class InvalidSyntax { public static void main(String[] args) { } ";
        Files.write(invalidJavaFile, invalidCode.getBytes());
        assertThrows(IOException.class, () -> {
            Java.compileJavaToClass(invalidJavaFile, tempDir);
        });
    }

    @Test
    public void testConvertJavaToSmali_NullCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            Java.convertJavaToSmali(null);
        });
    }
}
