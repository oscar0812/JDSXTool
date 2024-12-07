package io.github.oscar0812.JDSX.converters;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SmaliTest {

    private Path smaliFile;
    private String smaliCode;
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("smaliTest");
        smaliCode = ".class public LTestClass;\n" +
                ".super Ljava/lang/Object;\n" +
                ".source \"TestClass.java\"\n" +
                "\n" +
                ".method public constructor <init>()V\n" +
                "  .registers 1\n" +
                "  .prologue\n" +
                "  .line 1\n" +
                "    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V\n" +
                "    return-void\n" +
                ".end method\n" +
                "\n" +
                ".method public static main([Ljava/lang/String;)V\n" +
                "  .registers 3\n" +
                "  .prologue\n" +
                "  .line 3\n" +
                "    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;\n" +
                "    const-string v1, \"Hello, World!\"\n" +
                "    invoke-virtual { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V\n" +
                "  .line 4\n" +
                "    return-void\n" +
                ".end method";
        smaliFile = Smali.createTempSmaliFile(smaliCode);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up any files created during tests
        Files.walk(tempDir)
                .map(Path::toFile)
                .forEach(file -> {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                });
    }

    @Test
    public void testCreateTempSmaliFile_Success() {
        assertNotNull(smaliFile);
        assertTrue(Files.exists(smaliFile));
        assertTrue(smaliFile.toString().endsWith(".smali"));
    }

    @Test
    public void testCreateTempSmaliFile_EmptyCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            Smali.createTempSmaliFile("");
        });
    }

    @Test
    public void testCreateTempSmaliFile_NullCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            Smali.createTempSmaliFile(null);
        });
    }

    @Test
    public void testConvertSmaliToDex_Success() throws IOException {
        Path dexPath = tempDir.resolve("output.dex");
        Path result = Smali.convertSmaliToDex(smaliFile, dexPath);
        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertTrue(result.toString().endsWith(".dex"));
    }

    @Test
    public void testConvertSmaliToDex_NullSmaliPath() {
        Path dexPath = tempDir.resolve("output.dex");
        assertThrows(IllegalArgumentException.class, () -> {
            Smali.convertSmaliToDex((Path) null, dexPath);
        });
    }

    @Test
    public void testConvertSmaliToDex_NullDexPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            Smali.convertSmaliToDex(smaliFile, null);
        });
    }

    @Test
    public void testConvertSmaliToDex_ExceptionHandling() throws IOException {
        Path invalidSmaliPath = tempDir.resolve("Invalid.smali");
        Files.write(invalidSmaliPath, "invalid smali code".getBytes());
        Path dexPath = tempDir.resolve("output.dex");

        Smali.convertSmaliToDex(invalidSmaliPath, dexPath);
    }

    @Test
    public void testConvertSmaliToDex_SuccessString() throws IOException {
        Path dexPath = tempDir.resolve("output.dex");
        Path result = Smali.convertSmaliToDex(smaliCode, dexPath);
        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertTrue(result.toString().endsWith(".dex"));
    }

    @Test
    public void testConvertSmaliToDex_EmptyString() throws IOException {
        Path dexPath = tempDir.resolve("output.dex");

        assertThrows(IllegalArgumentException.class, () -> {
            Smali.convertSmaliToDex("", dexPath);
        });
    }

    @Test
    public void testConvertSmaliToDex_AutoOutputDir_Success() throws IOException {
        Path dexPath = Smali.convertSmaliToDex(smaliFile);
        assertNotNull(dexPath);
        assertTrue(Files.exists(dexPath));
        assertTrue(dexPath.toString().endsWith(".dex"));
    }

    @Test
    public void testConvertSmaliToClassJar_Success() throws IOException {
        Path jarPath = tempDir.resolve("output.jar");
        Path result = Smali.convertSmaliToClassJar(".class public Lcom/example/Test;", jarPath);
        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertTrue(result.toString().endsWith(".jar"));
    }

    @Test
    public void testConvertSmaliToClassJar_NullCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            Smali.convertSmaliToClassJar(null, tempDir.resolve("output.jar"));
        });
    }

    @Test
    public void testConvertSmaliToClasses_Success() throws IOException {
        Path result = Smali.convertSmaliToClasses(smaliCode);
        assertNotNull(result);
        assertTrue(Files.exists(result));
    }

    @Test
    public void testConvertSmaliToJava_Success() throws IOException {
        Path javaDir = Smali.convertSmaliToJava(smaliCode);
        assertNotNull(javaDir);
        assertTrue(Files.exists(javaDir));

        List<Path> javaPaths = FileUtils.findFilesByExtension(javaDir, ".java");
        Path javaPath = javaPaths.get(0);

        String expectedJavaCode = """
                public class TestClass {
                   public static void main(String[] var0) {
                      System.out.println("Hello, World!");
                   }
                }""";

        String expectedNormalized = expectedJavaCode.replaceAll("\r\n|\r|\n", "\n").trim();
        String actualNormalized = FileUtils.readFileToString(javaPath).replaceAll("\r\n|\r|\n", "\n").trim();
        assertEquals(expectedNormalized, actualNormalized);
    }

    @Test
    public void testConvertSmaliToJava_ExternalSynthetic_Success() throws Exception {
        Path javaDir = Smali.convertSmaliToJava("""
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
                .end method""");
        assertNotNull(javaDir);
        assertTrue(Files.exists(javaDir));

        List<Path> javaPaths = FileUtils.findFilesByExtension(javaDir, ".java");
        Path javaPath = javaPaths.get(0);

        String expectedJavaCode = """
                package com.example;
                
                import java.util.List;
                import java.util.function.Function;
                
                public final class Main$$ExternalSyntheticLambda0 implements Function {
                   public final Object apply(Object var1) {
                      return Main.lambda$main$0((List)var1);
                   }
                }""";

        String expectedNormalized = expectedJavaCode.replaceAll("\r\n|\r|\n", "\n").trim();
        String actualNormalized = FileUtils.readFileToString(javaPath).replaceAll("\r\n|\r|\n", "\n").trim();
        assertEquals(expectedNormalized, actualNormalized);
    }
}
