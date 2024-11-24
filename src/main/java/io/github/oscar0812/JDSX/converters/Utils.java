package io.github.oscar0812.JDSX.converters;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class Utils {

    public static Path generateSiblingPath(Path path, String extension) {
        String fileNameWithoutExt = path.getFileName().toString().replaceFirst("[.][^.]+$", "");
        return path.toAbsolutePath().getParent().resolve(fileNameWithoutExt + extension);
    }

    public static void validateFilePath(Path path, String description) {
        if (path == null) {
            throw new IllegalArgumentException(description + " cannot be null.");
        }
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(description + " does not exist: " + path);
        }
    }

    public static Path createTempDirectory() throws IOException {
        // Create a temporary directory for all files
        Path tempDir = Files.createTempDirectory("JavaToSmali");
        tempDir.toFile().deleteOnExit();  // Ensure it gets deleted when the JVM exits
        return tempDir;
    }

    public static Path[] getFiles(Path smaliDir, String extension) throws IOException {
        List<Path> smaliFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(smaliDir, "*." + extension)) {
            for (Path entry : stream) {
                smaliFiles.add(entry);
            }
        }
        return smaliFiles.toArray(new Path[0]);
    }
}
