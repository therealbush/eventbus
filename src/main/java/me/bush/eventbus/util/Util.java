package me.bush.eventbus.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * @author bush
 * @since 11/28/2021
 */
public class Util {

    /**
     * Sends log info about common exception types caught when trying to instantiate event handlers
     * with {@link Class#getDeclaredConstructor} and {@link Constructor#newInstance}, or when
     * calling {@link Method#invoke}.
     *
     * @param exception The exception that was caught.
     * @param name      The simple name of the class/method you are trying to instantiate/call.
     */
    public static void logReflectionExceptions(Exception exception, String name, Consumer<String> logger) {
        switch (exception.getClass().getSimpleName()) {
            case "IllegalAccessException":
                // Private constructor/method or other access error
                logger.accept(name + " could not be accessed.");
                break;
            case "InstantiationException":
                // Could not instantiate (abstract class, interface, etc).
                logger.accept(name + " could not be instantiated.");
                break;
            case "InvocationTargetException":
                // Exception thrown inside the constructor/method
                logger.accept(name + " threw an exception.");
                break;
            case "NoSuchMethodException":
                // No constructor found
                logger.accept(name + " has an incorrect constructor. See me.bush.eventbus.handler.Handler");
                break;
        }
    }

    /**
     * Nicer format of {@link Method#getName}. Ex: returns "ASMListener$ASMLoader#define" instead of "define".
     */
    public static String formatMethodName(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        // Null safety
        String packageName = clazz.getPackage() != null ? clazz.getPackage().getName() : "";
        // Cuts the package name off and adds a "#" before the method name
        return clazz.getName().replace(packageName + ".", "") + "#" + method.getName();
    }

    /**
     * Nicer format of {@link Class#getSimpleName}. Ex: returns "PacketEvent$Pre" instead of "Pre".
     */
    public static String formatClassName(Class<?> clazz) {
        // Null safety
        String packageName = clazz.getPackage() != null ? clazz.getPackage().getName() : "";
        // Cut off package name
        return clazz.getName().replace(packageName + ".", "");
    }
}
