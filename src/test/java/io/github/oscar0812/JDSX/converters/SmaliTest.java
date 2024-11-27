package io.github.oscar0812.JDSX.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SmaliTest {

    private Path smaliFile;
    private String smaliCode;
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("smaliTest");
        smaliCode = ".class public Lcom/example/Test;\n" +
                ".super Ljava/lang/Object;\n" +
                ".method public <init>()V\n" +
                "    .locals 1\n" +
                "    return-void\n" +
                ".end method";
        smaliFile = Smali.createTempSmaliFile(smaliCode);
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
    public void testConvertSmaliToJar_Success() throws IOException {
        Path jarPath = tempDir.resolve("output.jar");
        Path result = Smali.convertSmaliToJar(".class public Lcom/example/Test;", jarPath);
        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertTrue(result.toString().endsWith(".jar"));
    }

    @Test
    public void testConvertSmaliToJar_NullCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            Smali.convertSmaliToJar(null, tempDir.resolve("output.jar"));
        });
    }

    @Test
    public void testConvertSmaliToClasses_Success() throws IOException {
        Path result = Smali.convertSmaliToClasses(".class public Lcom/example/Test;");
        assertNotNull(result);
        assertTrue(Files.exists(result));
    }

    @Test
    public void testConvertSmaliToJava_Success() throws IOException {
        Path result = Smali.convertSmaliToJava(".class public Lcom/example/Test;");
        assertNotNull(result);
        assertTrue(Files.exists(result));
    }
}
