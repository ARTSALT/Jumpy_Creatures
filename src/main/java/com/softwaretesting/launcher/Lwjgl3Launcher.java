package com.softwaretesting.launcher;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.softwaretesting.adapters.persistence.DatabaseFactory;
import com.softwaretesting.adapters.persistence.H2ConnectionProvider;
import com.softwaretesting.adapters.ui.LibGdxApplication;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static LibGdxApplication game;

    public static LibGdxApplication launch(DatabaseFactory databaseFactory) {
        if (StartupHelper.startNewJvmIfRequired()) return null; // This handles macOS support and helps on Windows.
        return createApplication(databaseFactory);
    }

    private static LibGdxApplication createApplication(DatabaseFactory databaseFactory) {
        new Lwjgl3Application(game = new LibGdxApplication(databaseFactory), getDefaultConfiguration());
        return game;
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

    public static void main(String[] args) {
        try {
            // configuração do provedor de conexão H2
            DatabaseFactory databaseFactory = new DatabaseFactory(
                H2ConnectionProvider.builder().build()
            );

            // inicializa o jogo com o serviço de usuário
            launch(databaseFactory);

            // finaliza a conexão com o banco de dados após o uso
            databaseFactory.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println("Erro na execução da aplicação: " + e.getMessage());
        }
    }
}
