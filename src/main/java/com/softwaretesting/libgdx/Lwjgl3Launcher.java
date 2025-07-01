package com.softwaretesting.libgdx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.softwaretesting.database.UserService;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void launch(UserService userService) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication(userService);
    }

    private static void createApplication(UserService userService) {
        new Lwjgl3Application(new LibGdxApplication(userService), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Jumpy Creatures");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(1280, 720);
        configuration.setWindowIcon("images/libgdx128.png", "images/libgdx64.png", "images/libgdx32.png", "images/libgdx16.png");
        configuration.setDecorated(true);

        return configuration;
    }
}
