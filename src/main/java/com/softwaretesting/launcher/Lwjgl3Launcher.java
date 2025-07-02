package com.softwaretesting.launcher;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.softwaretesting.adapters.persistence.DatabaseFactory;
import com.softwaretesting.adapters.ui.LibGdxApplication;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void launch(DatabaseFactory databaseFactory) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication(databaseFactory);
    }

    private static void createApplication(DatabaseFactory databaseFactory) {
        new Lwjgl3Application(new LibGdxApplication(databaseFactory), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Jumpy Creatures");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(1280, 720);
        configuration.setWindowSizeLimits(1132, 600, -1, -1);
        configuration.setWindowIcon("images/libgdx128.png", "images/libgdx64.png", "images/libgdx32.png", "images/libgdx16.png");
        configuration.setDecorated(true);

        return configuration;
    }
}
