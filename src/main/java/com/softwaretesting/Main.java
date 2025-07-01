package com.softwaretesting;

import com.softwaretesting.database.DatabaseFactory;
import com.softwaretesting.database.H2ConnectionProvider;
import com.softwaretesting.database.UserService;
import com.softwaretesting.libgdx.Lwjgl3Launcher;

public class Main {
    public static void main(String[] args) {
        try {
            // configuração do provedor de conexão H2
            DatabaseFactory databaseFactory = new DatabaseFactory(
                H2ConnectionProvider.builder().build()
            );

            // obtém o serviço de usuário, que inicializa o banco de dados
            UserService userService = databaseFactory.getUserService();

            // inicializa o jogo com o serviço de usuário
            Lwjgl3Launcher.launch(userService);

            // finaliza a conexão com o banco de dados após o uso
            databaseFactory.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println("Erro na execução da aplicação: " + e.getMessage());
        }
    }
}
