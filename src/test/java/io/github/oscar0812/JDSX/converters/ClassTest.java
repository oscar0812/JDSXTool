package io.github.oscar0812.JDSX.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ClassTest {

    private Path tempDir;
    private Map<String, Path> fileMap;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("dex-test");
        fileMap = copyAllFilesToTemp();
    }

    @Test
    void testConvertClassFilesToDex_ValidClassFile() throws IOException {
        Path javaPath = fileMap.get("HelloWorld.java");
        Path classPath = Java.compileJavaToClass(javaPath);

        Path dexPath = Class.convertClassFilesToDex(classPath);
        assertTrue(Files.exists(dexPath));
    }

    @Test
    void testConvertClassFilesToDex_ValidClassFile_NotInTempDir() throws IOException {
        Path classPath = Paths.get("src/test/resources/files/TestClass.class");

        Path dexPath = Class.convertClassFilesToDex(classPath);
        assertTrue(Files.exists(dexPath));
    }

    @Test
    void testConvertClassFilesToDex_EmptyClassFile() {
        Path invalidClassFile = fileMap.get("Empty.class");

        assertThrows(RuntimeException.class, () -> {
            Class.convertClassFilesToDex(invalidClassFile);
        });
    }

    private Map<String, Path> copyAllFilesToTemp() throws IOException {
        Path resourceDir = Paths.get("src/test/resources/files");
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
