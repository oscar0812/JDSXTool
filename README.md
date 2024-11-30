# JDSXTool

JDSXTool is a powerful Java library designed to provide seamless conversions between Java, Smali, and other formats such as JAR and DEX. It is aimed at simplifying the reverse engineering process for Android applications, enabling the extraction, modification, and conversion of their contents.

## Features

- **Smali to Java**: Convert Smali code back to Java source code.
- **Java to Smali**: Convert Java source code into Smali code.
- **JAR and DEX Handling**: Handle JAR and DEX file formats, including extracting contents and converting between these formats.
- **Class to Smali**: Convert compiled Java classes into Smali.
- **Dex to Smali/Java**: Convert DEX files into Smali or Java code.

## Setup

You can include JDSXTool in your project via JitPack (https://jitpack.io/#oscar0812/JDSXTool) by adding the following dependency to your `build.gradle` file:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.oscar0812:JDSXTool:version_here'
}
```

## Example Usage

### Smali to Java

Convert Smali code to Java source code:

```java
import io.github.oscar0812.JDSX.converters.Smali;

Path smaliPath = Paths.get("path/to/smali/file.smali");
Path javaOutput = Smali.convertSmaliToJava(smaliPath);
System.out.println("Java file generated at: " + javaOutput);
```

### Java to Smali

Convert Java source code to Smali code:

```java
import io.github.oscar0812.JDSX.converters.Java;

Path javaPath = Paths.get("path/to/java/file.java");
Path smaliOutput = Java.convertJavaToSmali(javaPath);
System.out.println("Smali file generated at: " + smaliOutput);
```

### Convert Smali to DEX

Convert Smali to a DEX file:

```java
import io.github.oscar0812.JDSX.converters.Smali;

Path smaliPath = Paths.get("path/to/smali/files");
Path dexOutput = Smali.convertSmaliToDex(smaliPath);
System.out.println("DEX file generated at: " + dexOutput);
```

### Convert Java to DEX

Convert Java source code to a DEX file:

```java
import io.github.oscar0812.JDSX.converters.Java;

Path javaPath = Paths.get("path/to/java/file.java");
Path dexOutput = Java.convertJavaToDex(javaPath);
System.out.println("DEX file generated at: " + dexOutput);
```

### Convert Class File to Smali

Convert a class file to Smali code:

```java
import io.github.oscar0812.JDSX.converters.Class;

Path classPath = Paths.get("path/to/class/file.class");
Path smaliOutput = Class.convertClassFilesToSmali(classPath);
System.out.println("Smali file generated at: " + smaliOutput);
```

### Convert Dex to Java

Convert DEX to Java code:

```java
import io.github.oscar0812.JDSX.converters.Dex;

Path dexPath = Paths.get("path/to/dex/file.dex");
Path javaOutput = Dex.convertDexToJava(dexPath);
System.out.println("Java file generated at: " + javaOutput);
```

### Convert Class Files to DEX

Convert class files into a DEX file:

```java
import io.github.oscar0812.JDSX.converters.Class;

Path[] classPaths = {Paths.get("path/to/class1.class"), Paths.get("path/to/class2.class")};
Path dexOutput = Class.convertClassFilesToDex(classPaths);
System.out.println("DEX file generated at: " + dexOutput);
```

## Documentation

### `Smali.convertSmaliToJava(Path smaliPath)`

Converts a Smali file to a Java source file.

**Parameters:**
- `smaliPath`: The path to the Smali file to be converted.

**Returns:**
- Path to the generated Java source file.

---

### `Java.convertJavaToSmali(Path javaPath)`

Converts a Java source file to Smali code.

**Parameters:**
- `javaPath`: The path to the Java file to be converted.

**Returns:**
- Path to the generated Smali file.

---

### `Smali.convertSmaliToDex(Path smaliPath)`

Converts a Smali file to a DEX file.

**Parameters:**
- `smaliPath`: The path to the Smali file to be converted.

**Returns:**
- Path to the generated DEX file.

---

### `Java.convertJavaToDex(Path javaPath)`

Converts a Java source file to a DEX file.

**Parameters:**
- `javaPath`: The path to the Java file to be converted.

**Returns:**
- Path to the generated DEX file.

---

### `Class.convertClassFilesToSmali(Path classPath)`

Converts a class file to Smali code.

**Parameters:**
- `classPath`: The path to the class file to be converted.

**Returns:**
- Path to the generated Smali file.

---

### `Dex.convertDexToJava(Path dexPath)`

Converts a DEX file to Java code.

**Parameters:**
- `dexPath`: The path to the DEX file to be converted.

**Returns:**
- Path to the generated Java source file.

---

### `Class.convertClassFilesToDex(Path[] classPaths)`

Converts class files to a DEX file.

**Parameters:**
- `classPaths`: An array of paths to class files to be converted.

**Returns:**
- Path to the generated DEX file.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
