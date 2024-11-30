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
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        Path classFile = fileMap.get("HelloWorld.class");
        assertNotNull(classFile);

        Path dexPath = tempDir.resolve("output.dex");

        Class.convertClassFilesToDex(new Path[]{classFile}, dexPath);

        assertTrue(Files.exists(dexPath));
    }

    @Test
    void testConvertClassFilesToDex_EmptyClassFile() throws IOException {
        Path invalidClassFile = tempDir.resolve("Empty.class");

        Path dexPath = tempDir.resolve("output.dex");
        Class.convertClassFilesToDex(new Path[]{invalidClassFile}, dexPath);
        assertFalse(Files.exists(dexPath));
    }

    @Test
    void testConvertClassFilesToDex_NullClassFile() {
        Path dexPath = tempDir.resolve("output.dex");

        assertThrows(IllegalArgumentException.class, () -> Class.convertClassFilesToDex((Path[])null, dexPath));
    }

    @Test
    void testConvertClassFilesToDex_NullOutputDexPath() throws IOException {
        Path classFile = fileMap.get("HelloWorld.class");

        assertThrows(IllegalArgumentException.class, () -> Class.convertClassFilesToDex(new Path[]{classFile}, null));
    }

    @Test
    void testConvertClassFilesToDex_AutoOutputDir_ValidClassFile() throws IOException {
        Path classFile = fileMap.get("HelloWorld.class");
        assertNotNull(classFile);

        Path dexPath = Class.convertClassFilesToDex(classFile);

        assertTrue(Files.exists(dexPath));
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
