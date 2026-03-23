package org.min.app;

/**
 * Точка входа, отдельная от JavaFX Application.
 * Это обязательный workaround: если main-класс напрямую наследует Application,
 * JVM не находит JavaFX runtime до инициализации модулей.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}