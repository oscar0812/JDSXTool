package io.github.oscar0812.JDSX.converters;

import com.googlecode.d2j.smali.SmaliCmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for converting Smali code to different formats such as DEX, JAR, Classes, and Java.
 */
public class Smali {

    /**
     * Creates a temporary file containing the given Smali code.
     *
     * @param smaliCode the Smali code to be written to the file
     * @return the path to the created temporary Smali file
     * @throws IOException if an I/O error occurs during file creation or writing
     * @throws IllegalArgumentException if the provided Smali code is null or empty
     */
    public static Path createTempSmaliFile(String smaliCode) throws IOException {
        if (smaliCode == null || smaliCode.isEmpty()) {
            throw new IllegalArgumentException("Smali code cannot be null or empty.");
        }
        Path tempDir = Files.createTempDirectory("smali_temp");
        Path smaliFile = tempDir.resolve("TempSmali.smali");
        Files.write(smaliFile, smaliCode.getBytes());
        return smaliFile;
    }

    /**
     * Converts a Smali file to a DEX file.
     *
     * @param smaliPath the path to the Smali file
     * @param dexPath the path where the resulting DEX file will be saved
     * @return the path to the generated DEX file
     * @throws IOException if an I/O error occurs during conversion
     * @throws IllegalArgumentException if the DEX output path is null
     */
    public static Path convertSmaliToDex(Path smaliPath, Path dexPath) throws IOException {
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

    /**
     * Converts Smali code to a DEX file.
     *
     * @param smaliCode the Smali code to be converted
     * @param dexPath the path where the resulting DEX file will be saved
     * @return the path to the generated DEX file
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToDex(String smaliCode, Path dexPath) throws IOException {
        Path smaliPath = createTempSmaliFile(smaliCode);
        return convertSmaliToDex(smaliPath, dexPath);
    }

    /**
     * Converts Smali code to a JAR file.
     *
     * @param smaliCode the Smali code to be converted
     * @param jarPath the path where the resulting JAR file will be saved
     * @return the path to the generated JAR file
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToJar(String smaliCode, Path jarPath) throws IOException {
        Path smaliPath = createTempSmaliFile(smaliCode);
        Path dexPath = Utils.generateSiblingPath(jarPath, ".dex");
        convertSmaliToDex(smaliPath, dexPath);
        return Dex.convertDexToJar(dexPath, jarPath);
    }

    /**
     * Converts Smali code to class files by first converting it to a JAR file and then extracting the classes.
     *
     * @param smaliCode the Smali code to be converted
     * @return the path to the extracted classes
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToClasses(String smaliCode) throws IOException {
        Path smaliPath = createTempSmaliFile(smaliCode);
        Path jarPath = Utils.generateSiblingPath(smaliPath, ".jar");
        jarPath = convertSmaliToJar(smaliCode, jarPath);
        return Jar.extractJar(jarPath);
    }

    /**
     * Converts Smali code to Java code by first converting it to a JAR file and then decompiling the JAR to Java.
     *
     * @param smaliCode the Smali code to be converted
     * @return the path to the generated Java code
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToJava(String smaliCode) throws IOException {
        Path smaliPath = createTempSmaliFile(smaliCode);
        Path jarPath = Utils.generateSiblingPath(smaliPath, ".jar");
        jarPath = convertSmaliToJar(smaliCode, jarPath);
        return Jar.convertClassJarToJavaJar(jarPath);
    }
}