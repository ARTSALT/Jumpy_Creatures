package com.softwaretesting.launcher;

import com.softwaretesting.adapters.persistence.DatabaseFactory;
import com.softwaretesting.adapters.persistence.H2ConnectionProvider;

public class Main {
    public static void main(String[] args) {
        try {
            // configuração do provedor de conexão H2
            DatabaseFactory databaseFactory = new DatabaseFactory(
                H2ConnectionProvider.builder().build()
            );

            // inicializa o jogo com o serviço de usuário
            Lwjgl3Launcher.launch(databaseFactory);

            // finaliza a conexão com o banco de dados após o uso
            databaseFactory.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println("Erro na execução da aplicação: " + e.getMessage());
        }
    }
}
