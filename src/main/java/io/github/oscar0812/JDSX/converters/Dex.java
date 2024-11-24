package io.github.oscar0812.JDSX.converters;

import com.android.dx.command.dexer.DxContext;
import com.android.dx.command.dexer.Main;
import com.googlecode.d2j.smali.BaksmaliCmd;
import com.googlecode.dex2jar.tools.Dex2jarCmd;

import java.io.IOException;
import java.nio.file.Path;


public class Dex {

    public static Path convertDexToJar(Path dexPath, Path jarPath) {
        Utils.validateFilePath(dexPath, "Dex path");
        if (jarPath == null) {
            throw new IllegalArgumentException("JAR output path cannot be null.");
        }
        try {
            Dex2jarCmd.main(dexPath.toString(), "-o", jarPath.toString(), "--force");
        } catch (Exception e) {
            throw new RuntimeException("Error converting Dex to JAR", e);
        }
        return jarPath;
    }

    public static void convertClassFilesToDex(String[] inputClassFilePaths, String outputDexPath) {
        DxContext dxContext = new DxContext();

        Main.Arguments arguments = new Main.Arguments();
        arguments.outName = outputDexPath;
        arguments.strictNameCheck = false;
        arguments.fileNames = inputClassFilePaths;

        try {
            new Main(dxContext).runDx(arguments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void convertDexToSmali(String dexFilePath, String outputDir) {
        String[] args = {dexFilePath, "-o", outputDir, "--force"};
        BaksmaliCmd.main(args);
    }
}
