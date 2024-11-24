package io.github.oscar0812.JDSX.converters;

import com.googlecode.d2j.smali.SmaliCmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Smali {

    public static Path createTempSmaliFile(String smaliCode) throws IOException {
        if (smaliCode == null || smaliCode.isEmpty()) {
            throw new IllegalArgumentException("Smali code cannot be null or empty.");
        }
        Path tempDir = Files.createTempDirectory("smali_temp");
        Path smaliFile = tempDir.resolve("TempSmali.smali");
        Files.write(smaliFile, smaliCode.getBytes());
        return smaliFile;
    }

    public static Path convertSmaliToDex(Path smaliPath, Path dexPath) {
        Utils.validateFilePath(smaliPath, "Smali path");
        if (dexPath == null) {
            throw new IllegalArgumentException("Dex output path cannot be null.");
        }
        try {
            SmaliCmd.main(smaliPath.toString(), "-o", dexPath.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error converting Smali to Dex", e);
        }
        return dexPath;
    }

    public static Path convertSmaliToDex(String smaliCode, Path dexPath) throws IOException {
        Path smaliPath = createTempSmaliFile(smaliCode);
        return convertSmaliToDex(smaliPath, dexPath);
    }

    public static Path convertSmaliToJar(String smaliCode, Path jarPath) throws IOException {
        Path smaliPath = createTempSmaliFile(smaliCode);
        Path dexPath = Utils.generateSiblingPath(jarPath, ".dex");
        convertSmaliToDex(smaliPath, dexPath);
        return Dex.convertDexToJar(dexPath, jarPath);
    }

    public static Path convertSmaliToClasses(String smaliCode) throws IOException {
        Path jarPath = convertSmaliToJar(smaliCode, Utils.generateSiblingPath(Path.of("output"), ".jar"));
        return Jar.extractJar(jarPath);
    }

    public static Path convertSmaliToJava(String smaliCode) throws IOException {
        Path jarPath = convertSmaliToJar(smaliCode, Utils.generateSiblingPath(Path.of("output"), ".jar"));
        return Jar.convertClassJarToJavaJar(jarPath);
    }
}
