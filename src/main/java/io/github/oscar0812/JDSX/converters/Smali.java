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
        Path tempDir = Utils.createTempDirectory("smali_temp");
        Path smaliFile = tempDir.resolve("TempSmali.smali");
        Files.write(smaliFile, smaliCode.getBytes());
        return smaliFile;
    }

    /**
     * Converts Smali code to a DEX file.
     * Smali -> Dex
     *
     * @param smaliCode the Smali code to be converted
     * @return the path to the generated DEX file
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToDex(String smaliCode) throws IOException {
        Path smaliPath = createTempSmaliFile(smaliCode);
        Path dexPath = Utils.getSiblingPath(smaliPath, ".dex");
        return convertSmaliToDex(smaliPath, dexPath);
    }

    /**
     * Converts Smali code to a DEX file.
     * Smali -> Dex
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
     * Converts a Smali file to a DEX file.
     * Smali -> Dex
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
     * Converts a Smali file to a DEX file.
     * Smali -> Dex
     *
     * @param smaliPath the path to the Smali file
     * @return the path to the generated DEX file
     * @throws IOException if an I/O error occurs during conversion
     * @throws IllegalArgumentException if the DEX output path is null
     */
    public static Path convertSmaliToDex(Path smaliPath) throws IOException {
        Utils.validateFilePath(smaliPath, "Smali path");
        Path dexPath = Utils.getSiblingPath(smaliPath, "output.dex");
        return convertSmaliToDex(smaliPath, dexPath);
    }

    /**
     * Converts Smali code to a JAR file.
     * Smali -> Dex -> Class Jar
     *
     * @param smaliCode the Smali code to be converted
     * @return the path to the generated JAR file
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToClassJar(String smaliCode) throws IOException {
        Path smaliPath = createTempSmaliFile(smaliCode);
        return convertSmaliToClassJar(smaliPath);
    }

    /**
     * Converts a Smali file to a JAR file.
     * Smali -> Dex -> Class Jar
     *
     * @param smaliPath the Smali code path to be converted
     * @return the path to the generated JAR file
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToClassJar(Path smaliPath) throws IOException {
        Path dexPath = convertSmaliToDex(smaliPath);
        return Dex.convertDexToClassJar(dexPath);
    }

    /**
     * Converts Smali code to a JAR file.
     * Smali -> Dex -> Class Jar
     *
     * @param smaliCode the Smali code to be converted
     * @param jarPath the path where the resulting JAR file will be saved
     * @return the path to the generated JAR file
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToClassJar(String smaliCode, Path jarPath) throws IOException {
        Path smaliPath = createTempSmaliFile(smaliCode);
        Path dexPath = convertSmaliToDex(smaliPath);
        return Dex.convertDexToClassJar(dexPath, jarPath);
    }

    /**
     * Converts Smali code to class files
     * Smali -> Class Jar -> Extract
     *
     * @param smaliCode the Smali code to be converted
     * @return the path to the extracted classes
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToClasses(String smaliCode) throws IOException {
        Path jarPath = convertSmaliToClassJar(smaliCode);
        return Jar.extractJar(jarPath);
    }

    /**
     * Converts Smali code to class files
     * Smali -> Class Jar -> Extract
     *
     * @param smaliPath the Smali path code to be converted
     * @return the path to the extracted classes
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToClasses(Path smaliPath) throws IOException {
        Path jarPath = convertSmaliToClassJar(smaliPath);
        return Jar.extractJar(jarPath);
    }

    /**
     * Converts Smali code to Java code
     * Smali -> Dex -> Class Jar -> Java Jar
     *
     * @param smaliCode the Smali code to be converted
     * @return the path to the generated Java code
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToJava(String smaliCode) throws IOException {
        Path dexPath = Smali.convertSmaliToDex(smaliCode);
        Path classJarPath = Dex.convertDexToClassJar(dexPath);
        return Jar.convertClassJarToJava(classJarPath);
    }

    /**
     * Converts Smali code to Java code
     * Smali -> Dex -> Class Jar -> Java Jar
     *
     * @param smaliPath the Smali code path to be converted
     * @return the path to the generated Java code
     * @throws IOException if an I/O error occurs during conversion
     */
    public static Path convertSmaliToJava(Path smaliPath) throws IOException {
        Path dexPath = Smali.convertSmaliToDex(smaliPath);
        Path classJarPath = Dex.convertDexToClassJar(dexPath);
        return Jar.convertClassJarToJava(classJarPath);
    }
}
