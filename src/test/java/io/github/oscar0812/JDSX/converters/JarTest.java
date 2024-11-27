package io.github.oscar0812.JDSX.converters;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipException;

import static org.junit.jupiter.api.Assertions.*;

class JarTest {

    private Path tempDir;
    private Map<String, Path> fileMap;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("jar-test");
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
    void testExtractJar_ValidJarFile() throws IOException {
        Path jarFile = fileMap.get("test.jar");
        assertNotNull(jarFile);

        Path extractedDir = Jar.extractJar(jarFile);

        assertTrue(Files.exists(extractedDir));
        assertTrue(Files.list(extractedDir).findAny().isPresent());
    }

    @Test
    void testExtractJar_InvalidJarFile() {
        Path invalidJarFile = tempDir.resolve("invalid.jar");

        assertThrows(IOException.class, () -> Jar.extractJar(invalidJarFile));
    }

    @Test
    void testExtractJar_NullJarFile() {
        assertThrows(IllegalArgumentException.class, () -> Jar.extractJar((Path) null));
    }

    @Test
    void testExtractJar_EmptyJarFile() {
        Path emptyJarFile = tempDir.resolve("empty.jar");

        assertThrows(ZipException.class, () -> Jar.extractJar(emptyJarFile));
    }

    @Test
    void testExtractJar_NullDestinationDirectory() throws IOException {
        Path jarFile = fileMap.get("test.jar");

        assertThrows(IllegalArgumentException.class, () -> Jar.extractJar(jarFile, null));
    }

    @Test
    void testConvertClassJarToJavaJar_ValidJar() throws IOException {
        Path jarFile = fileMap.get("test.jar");
        assertNotNull(jarFile);

        Path outputDir = Jar.convertClassJarToJavaJar(jarFile);

        assertTrue(Files.exists(outputDir));
        assertTrue(Files.list(outputDir).anyMatch(path -> path.toString().endsWith(".java")));
    }

    @Test
    void testConvertClassJarToJavaJar_EmptyJar() {
        Path invalidJarFile = tempDir.resolve("empty.jar");

        assertThrows(IOException.class, () -> Jar.convertClassJarToJavaJar(invalidJarFile));
    }

    @Test
    void testConvertClassJarToJavaJar_MissingJar() {
        Path invalidJarFile = tempDir.resolve("invalid.jar");

        assertThrows(IOException.class, () -> Jar.convertClassJarToJavaJar(invalidJarFile));
    }

    @Test
    void testConvertClassJarToJavaJar_NullJarFile() {
        assertThrows(IllegalArgumentException.class, () -> Jar.convertClassJarToJavaJar(null));
    }

    @Test
    void testExtractJar_JarWithNestedDirectories() throws IOException {
        Path jarFile = fileMap.get("nested_dirs_test.jar");
        assertNotNull(jarFile);

        Path extractedDir = Jar.extractJar(jarFile);

        assertTrue(Files.exists(extractedDir));
        assertTrue(Files.list(extractedDir).anyMatch(path -> path.getParent().toString().contains("/nested")));
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
