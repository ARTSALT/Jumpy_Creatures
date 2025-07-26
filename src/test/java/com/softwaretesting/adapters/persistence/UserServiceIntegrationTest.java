package com.softwaretesting.adapters.persistence;

import com.softwaretesting.core.application.service.UserService;
import com.softwaretesting.core.domain.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de integração para a classe UserService, utilizando um banco de dados H2 em memória.
 * Foca na interação real entre o UserService e a camada de banco de dados.
 */
@DisplayName("Testes de Integração para UserService com Banco de Dados H2 em Memória")
public class UserServiceIntegrationTest {

    private DatabaseFactory databaseFactory;
    private UserService userService;

    /**
     * Configura um ambiente de teste limpo antes de cada teste.
     * Cria um novo provedor de conexão, uma nova fábrica e um novo serviço.
     */
    @BeforeEach
    void setup() throws SQLException, IOException {
        H2ConnectionProvider connectionProvider = H2ConnectionProvider.builder().useInMemory(true).build();
        this.databaseFactory = new DatabaseFactory(connectionProvider);
        this.userService = databaseFactory.getUserService();

        // limpa o banco de dados antes de cada teste
        connectionProvider.initializeDatabase(null);
        System.out.println("Setup completo: Banco de dados em memória inicializado.");
    }

    /**
     * Limpa o ambiente após cada teste, fechando a conexão com o banco.
     */
    @AfterEach
    void tearDown() throws SQLException {
        if (this.databaseFactory != null) {
            this.databaseFactory.close();
            System.out.println("Teardown completo: Conexão fechada, DB em memória descartado.");
        }
    }

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso")
    void shouldRegisterNewUserSuccessfully() throws SQLException {
        User user = new User("testUser4", "validPassword1234");

        boolean result = userService.register(user);
        boolean exists = userService.userExists(user);

        assertThat(result).isTrue();
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve impedir o registro de um usuário com o mesmo username")
    void shouldPreventRegisteringExistingUser() throws SQLException {
        User user = new User("existingUser", "anyPassword123");
        userService.register(user); // registra o usuário pela primeira vez

        // tenta registrar o mesmo usuário novamente
        assertThatThrownBy(() -> userService.register(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Username already exists.");
    }

    @Test
    @DisplayName("Deve permitir o login de um usuário registrado com a senha correta")
    void shouldAllowLoginForRegisteredUserWithCorrectPassword() throws SQLException {
        User userToRegister = new User("testUser", "correctPassword123");
        userService.register(userToRegister);

        User userToLogin = new User("testUser", "correctPassword123");

        Optional<User> loggedInUser = userService.login(userToLogin);

        assertThat(loggedInUser).isPresent();
        assertThat(loggedInUser.get().getUsername()).isEqualTo(userToRegister.getUsername());
    }

    @Test
    @DisplayName("Não deve permitir o login com uma senha incorreta")
    void shouldNotAllowLoginWithIncorrectPassword() throws SQLException {
        User userToRegister = new User("testUser5", "correctPassword12345");
        userService.register(userToRegister);

        User userToLogin = new User("testUser", "WRONG_Password123");

        Optional<User> loggedInUser = userService.login(userToLogin);

        assertThat(loggedInUser).isEmpty();
    }
}
