package com.softwaretesting.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de integração para a classe UserService, utilizando um banco de dados H2 em memória.
 * Foca na interação real com o banco de dados, testando o registro e login de usuários.
 */
public class UserServiceIntegrationTest {

    private UserService userService;
    private Connection testConnection;

    /**
     * Testes unitários para a classe UserService, utilizando um banco de dados H2 em memória.
     * Os testes incluem registro de usuário, login e verificação de usuários existentes.
     */
    @BeforeEach
    void setup() throws SQLException {
        testConnection = ConnectionFactory.getConnection(true); // true para usar DB em memória

        ConnectionFactory.initializeDatabase(testConnection);   // inicializa estrutura do bd

        UserRepository userRepository = new UserRepositoryImpl(testConnection);
        userService = new UserService(userRepository);

        System.out.println("Setup completo: Novo DB em memória inicializado para o teste.");
    }

    /**
     * Metodo de limpeza executado após cada teste.
     * Fecha a conexão com o banco de dados H2 em memória.
     */
    @AfterEach
    void close() {
        if (testConnection != null) {
            try {
                testConnection.close();
                System.out.println("Teardown completo: Conexão fechada, DB em memória descartado.");
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão de teste: " + e.getMessage());
            }
        }
    }

    /**
     * Testa o registro de um usuário, verificando se o usuário é registrado com sucesso
     * e se ele existe no banco de dados após o registro.
     */
    @Test
    public void testRegisterUser() {
        String username = "testUser";
        String password = "testPassword";

        assertThatNoException().isThrownBy(() -> userService.registerUser(username, password));
        assertThatNoException().isThrownBy(() -> assertThat(userService.userExists(username)).isTrue());
    }

    /**
     * Testa o login de um usuário registrado, verificando se o login é bem-sucedido
     */
    @Test
    public void testLoginUser() {
        String username = "testUser2";
        String password = "testPassword2";

        assertThatNoException().isThrownBy(() -> {
            boolean registrationResult = userService.registerUser(username, password);
            assertThat(registrationResult).isTrue();

            Optional<User> userOptional = userService.login(username, password);
            assertThat(userOptional.isPresent()).isTrue();
            assertThat(username).isEqualTo(userOptional.get().getUsername());
        });
    }

    /**
     * Testa o login de um usuário com senha incorreta, verificando se o login falha
     */
    @Test
    public void testLoginUserIncorrectPassword() throws SQLException {
        String username = "testUser3";
        String password = "testPassword3";
        userService.registerUser(username, password); // registra um usuário para o teste

        Optional<User> userOptional = userService.login(username, "wrongPassword");
        assertThat(userOptional.isPresent()).isFalse();
    }

    /**
     * Testa o login de um usuário não registrado, verificando se o login falha
     */
    @Test
    public void testRegisterExistingUser() throws SQLException {
        String username = "existingUser";
        String password = "anyPassword";
        userService.registerUser(username, password); // registra o usuário pela primeira vez

        // tenta registrar o mesmo usuário novamente
        assertThatThrownBy(() -> userService.registerUser(username, "anotherPassword"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
