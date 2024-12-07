package io.github.oscar0812.JDSX.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JavaTest {

    private String javaCode;
    private Path javaFile;
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("javaTest");
        javaFile = tempDir.resolve("TestClass.java");
        javaCode = """
                public class TestClass {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }""";
        Files.write(javaFile, javaCode.getBytes());
    }

    @Test
    public void testCompileJavaToClass_Success() throws IOException {
        Path classesDir = Java.compileJavaToClass(javaFile, tempDir);
        assertNotNull(classesDir);

        List<Path> classFiles = FileUtils.findAllFiles(classesDir);
        assertTrue(!classFiles.isEmpty());
        Path classFile = classFiles.get(0);
        assertTrue(classFile.toString().endsWith(".class"));
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
        Path smaliDir = Java.convertJavaToSmali(javaCode);
        assertNotNull(smaliDir);

        List<Path> smaliFiles = FileUtils.findFilesByExtension(smaliDir, ".smali");
        assertEquals(1, smaliFiles.size());

        Path smaliFile = smaliFiles.get(0);
        assertTrue(smaliFile.toString().endsWith(".smali"));

        String expectedSmali = """
                .class public LTestClass;
                .super Ljava/lang/Object;
                .source "TestClass.java"
                
                .method public constructor <init>()V
                  .registers 1
                  .line 1
                    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
                    return-void
                .end method
                
                .method public static main([Ljava/lang/String;)V
                  .registers 2
                  .line 3
                    sget-object p0, Ljava/lang/System;->out:Ljava/io/PrintStream;
                    const-string v0, "Hello, World!"
                    invoke-virtual { p0, v0 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
                  .line 4
                    return-void
                .end method""";

        String expectedNormalized = expectedSmali.replaceAll("\r\n|\r|\n", "\n").trim();
        String actualNormalized = FileUtils.readFileToString(smaliFile).replaceAll("\r\n|\r|\n", "\n").trim();
        assertEquals(expectedNormalized, actualNormalized);
    }

    @Test
    public void testConvertJavaToSmali_WithPackage_Success() throws Exception {
        Path smaliDir = Java.convertJavaToSmali("""
                package com.example.demo;
            
                public class HelloWorld {
                }""");
        assertNotNull(smaliDir);

        List<Path> smaliFiles = FileUtils.findFilesByExtension(smaliDir, ".smali");
        assertEquals(1, smaliFiles.size());

        Path smaliFile = smaliFiles.get(0);
        assertTrue(smaliFile.toString().endsWith(".smali"));

        String expectedSmali = """
               .class public Lcom/example/demo/HelloWorld;
               .super Ljava/lang/Object;
               .source "HelloWorld.java"
    
               .method public constructor <init>()V
                 .registers 1
                 .line 3
                   invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
                   return-void
               .end method""";

        String expectedNormalized = expectedSmali.replaceAll("\r\n|\r|\n", "\n").trim();
        String actualNormalized = FileUtils.readFileToString(smaliFile).replaceAll("\r\n|\r|\n", "\n").trim();
        assertEquals(expectedNormalized, actualNormalized);
    }


    @Test
    public void testConvertJavaToSmali_UsingLambda_Success() throws Exception {
        Path smaliDir = Java.convertJavaToSmali("""
                package com.example;
                
                import java.util.Arrays;
                import java.util.List;
                
                public class Main {
                    public static void main(String[] args) {
                
                        List<String> type = Arrays.asList(Arrays.asList(" ")).stream()
                                .flatMap(a -> a.stream()).toList();
                
                        System.out.println(type);
                
                        System.out.println("HELLO WORLD2");
                    }
                }""");
        assertNotNull(smaliDir);

        List<Path> smaliFiles = FileUtils.findFilesByExtension(smaliDir, ".smali");
        assertEquals(2, smaliFiles.size());

        Path smaliFile1 = smaliFiles.get(0);
        assertTrue(smaliFile1.toString().endsWith(".smali"));

        String expectedSmali1 = """
                .class public final synthetic Lcom/example/Main$$ExternalSyntheticLambda0;
                .super Ljava/lang/Object;
                .implements Ljava/util/function/Function;
                .source "D8$$SyntheticClass"
                
                .method public synthetic constructor <init>()V
                  .registers 1
                  .line 0
                    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
                    return-void
                .end method
                
                .method public final apply(Ljava/lang/Object;)Ljava/lang/Object;
                  .registers 2
                  .line 0
                    check-cast p1, Ljava/util/List;
                    invoke-static { p1 }, Lcom/example/Main;->lambda$main$0(Ljava/util/List;)Ljava/util/stream/Stream;
                    move-result-object p1
                    return-object p1
                .end method""";

        String expectedNormalized1 = expectedSmali1.replaceAll("\r\n|\r|\n", "\n").trim();
        String actualNormalized1 = FileUtils.readFileToString(smaliFile1).replaceAll("\r\n|\r|\n", "\n").trim();
        assertEquals(expectedNormalized1, actualNormalized1);

        // ==== the second smali
        Path smaliFile2 = smaliFiles.get(1);
        assertTrue(smaliFile2.toString().endsWith(".smali"));

        String expectedSmali2 = """
                .class public Lcom/example/Main;
                .super Ljava/lang/Object;
                .source "Main.java"
                
                .method public constructor <init>()V
                  .registers 1
                  .line 6
                    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
                    return-void
                .end method
                
                .method static synthetic lambda$main$0(Ljava/util/List;)Ljava/util/stream/Stream;
                  .registers 1
                  .line 10
                    invoke-interface { p0 }, Ljava/util/List;->stream()Ljava/util/stream/Stream;
                    move-result-object p0
                    return-object p0
                .end method
                
                .method public static main([Ljava/lang/String;)V
                  .registers 4
                  .line 9
                    const/4 p0, 1
                    new-array v0, p0, [Ljava/util/List;
                    new-array p0, p0, [Ljava/lang/String;
                    const-string v1, " "
                    const/4 v2, 0
                    aput-object v1, p0, v2
                    invoke-static { p0 }, Ljava/util/Arrays;->asList([Ljava/lang/Object;)Ljava/util/List;
                    move-result-object p0
                    aput-object p0, v0, v2
                    invoke-static { v0 }, Ljava/util/Arrays;->asList([Ljava/lang/Object;)Ljava/util/List;
                    move-result-object p0
                    invoke-interface { p0 }, Ljava/util/List;->stream()Ljava/util/stream/Stream;
                    move-result-object p0
                    new-instance v0, Lcom/example/Main$$ExternalSyntheticLambda0;
                    invoke-direct { v0 }, Lcom/example/Main$$ExternalSyntheticLambda0;-><init>()V
                  .line 10
                    invoke-interface { p0, v0 }, Ljava/util/stream/Stream;->flatMap(Ljava/util/function/Function;)Ljava/util/stream/Stream;
                    move-result-object p0
                    invoke-interface { p0 }, Ljava/util/stream/Stream;->toList()Ljava/util/List;
                    move-result-object p0
                  .line 12
                    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
                    invoke-virtual { v0, p0 }, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V
                  .line 14
                    sget-object p0, Ljava/lang/System;->out:Ljava/io/PrintStream;
                    const-string v0, "HELLO WORLD2"
                    invoke-virtual { p0, v0 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
                  .line 15
                    return-void
                .end method""";

        String expectedNormalized2 = expectedSmali2.replaceAll("\r\n|\r|\n", "\n").trim();
        String actualNormalized2 = FileUtils.readFileToString(smaliFile2).replaceAll("\r\n|\r|\n", "\n").trim();
        assertEquals(expectedNormalized2, actualNormalized2);
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

        List<Path> classFiles = FileUtils.findAllFiles(classesDir);
        assertFalse(classFiles.isEmpty());
        assertTrue(classFiles.get(0).toString().endsWith(".class"));
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
            Java.convertJavaToSmali((String) null);
        });
    }
}
