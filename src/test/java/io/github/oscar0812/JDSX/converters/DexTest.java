package io.github.oscar0812.JDSX.converters;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DexTest {

    private Path tempDir;
    private Map<String, Path> fileMap;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("dex-test");
        fileMap = copyAllFilesToTemp();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    void testConvertDexToJar_ValidDexFile() throws IOException {
        Path dexFile = fileMap.get("test.dex");
        assertNotNull(dexFile);

        Path jarPath = dexFile.resolveSibling("test.jar");

        Path result = Dex.convertDexToJar(dexFile);

        assertEquals(jarPath, result);
        assertTrue(Files.exists(jarPath));
    }

    @Test
    void testConvertDexToJar_InvalidDexFile() {
        Path invalidDexFile = tempDir.resolve("invalid.dex");

        assertThrows(IOException.class, () -> Dex.convertDexToJar(invalidDexFile));
    }

    @Test
    void testConvertDexToJar_NullDexFile() {
        assertThrows(IllegalArgumentException.class, () -> Dex.convertDexToJar((Path) null));
    }

    @Test
    void testConvertDexToJar_EmptyDexFile() throws IOException {
        Path emptyDexFile = tempDir.resolve("empty.dex");

        assertThrows(IllegalArgumentException.class, () -> Dex.convertDexToJar(emptyDexFile));
    }

    @ParameterizedTest
    @CsvSource({
            "nonexistent.dex, false",
            "test.dex, true"
    })
    void testIsValidDexFile(String dexFileName, boolean expected) {
        Path dexFile = fileMap.get(dexFileName);
        if (dexFile == null) {
            dexFile = tempDir.resolve(dexFileName);
        }
        assertEquals(expected, Dex.isValidDexFile(dexFile));
    }

    @Test
    void testConvertDexToSmali_ValidDex() throws IOException {
        Path dexFile = fileMap.get("test.dex");
        assertNotNull(dexFile);

        Path outputDir = tempDir.resolve("smali-output");

        Dex.convertDexToSmali(dexFile, outputDir);

        assertTrue(Files.isDirectory(outputDir));
        assertTrue(Files.list(outputDir).findAny().isPresent());
    }

    @Test
    void testConvertDexToSmali_InvalidDex() {
        Path invalidDexFile = tempDir.resolve("invalid.dex");

        Path outputDir = tempDir.resolve("smali-output");

        assertThrows(IOException.class, () -> Dex.convertDexToSmali(invalidDexFile, outputDir));
    }

    @Test
    void testConvertDexToSmali_NullDexFile() {
        Path outputDir = tempDir.resolve("smali-output");

        assertThrows(IllegalArgumentException.class, () -> Dex.convertDexToSmali(null, outputDir));
    }

    @Test
    void testConvertDexToSmali_NullOutputDir() {
        Path dexFile = fileMap.get("test.dex");

        assertThrows(IllegalArgumentException.class, () -> Dex.convertDexToSmali(dexFile, null));
    }

    @Test
    void testConvertDexToJar_NullOutputPath() {
        Path dexFile = fileMap.get("test.dex");

        assertThrows(IllegalArgumentException.class, () -> Dex.convertDexToJar(dexFile, null));
    }

    @Test
    void testConvertDexToSmali_AutoOutputDir_ValidDex() throws IOException {
        Path dexFile = fileMap.get("test.dex");
        assertNotNull(dexFile);

        // Expected sibling directory for Smali files
        Path expectedOutputDir = dexFile.resolveSibling("test_smali");

        // Call the method to test
        Dex.convertDexToSmali(dexFile);

        // Assert the output directory is created and contains files
        assertTrue(Files.isDirectory(expectedOutputDir));
        assertTrue(Files.list(expectedOutputDir).findAny().isPresent());
    }

    @Test
    void testConvertDexToSmali_AutoOutputDir_InvalidDex() {
        Path invalidDexFile = tempDir.resolve("invalid.dex");

        // Ensure no unexpected exceptions occur and directory is not created
        assertThrows(IOException.class, () -> Dex.convertDexToSmali(invalidDexFile));

        Path expectedOutputDir = invalidDexFile.resolveSibling("invalid_smali");
        assertFalse(Files.exists(expectedOutputDir));
    }

    @Test
    void testConvertDexToSmali_AutoOutputDir_NullDexFile() {
        assertThrows(IllegalArgumentException.class, () -> Dex.convertDexToSmali(null));
    }

    private Map<String, Path> copyAllFilesToTemp() throws IOException {
        Path resourceDir = Paths.get("src", "test", "resources", "files");
        Map<String, Path> fileMap = new HashMap<>();

        if (Files.exists(resourceDir) && Files.isDirectory(resourceDir)) {
            Files.walk(resourceDir)
                    .filter(Files::isRegularFile)
                    .forEach(sourcePath -> {
                        try {
                            Path targetPath = tempDir.resolve(sourcePath.getFileName().toString());
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            fileMap.put(sourcePath.getFileName().toString(), targetPath);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } else {
            throw new IOException("Resource directory not found: " + resourceDir);
        }

        return fileMap;
    }
}
