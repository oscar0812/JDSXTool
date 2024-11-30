import io.github.oscar0812.JDSX.converters.*;

import java.lang.Class;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ReflectionForClassList {
    public static void main(String[] args) {
        // List of classes to inspect
        Class<?>[] classes = {
                io.github.oscar0812.JDSX.converters.Class.class,
                Dex.class,
                Jar.class,
                Java.class,
                Smali.class
        };

        // Iterate over the list of classes
        for (Class<?> clazz : classes) {
            System.out.println("Declared methods of " + clazz.getName() + ":");

            // Get all declared methods of the current class
            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                System.out.print(method.getName() + "(");

                // Get parameters of the method
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    if (i > 0) {
                        System.out.print(", ");
                    }
                    Parameter parameter = parameters[i];
                    // Print parameter type and name
                    System.out.print(parameter.getType().getSimpleName() + " " + parameter.getName());
                }

                System.out.println(")");
            }
            System.out.println(); // Add space between classes
        }

    }
}