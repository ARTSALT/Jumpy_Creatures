package com.softwaretesting.adapters.ui;

import com.softwaretesting.launcher.Lwjgl3Launcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User Journeys")
public class UserJourneysTest {

    @Test
    @DisplayName("Inicializa o ambiente gráfico para rodar as jornadas de usuário")
    public void initializeGraphicsEnvironment() {
        Lwjgl3Launcher.main(new String[] {});
    }
}
