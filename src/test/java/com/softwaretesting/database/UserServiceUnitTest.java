package com.softwaretesting.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe UserService, utilizando mocks para a UserRepository.
 * Foca na lógica de negócio do UserService, isolando-o das interações reais com o banco de dados.
 */
public class UserServiceUnitTest {

    private UserRepository mockUserRepository;
    private UserService userService;

    /**
     * Configuração inicial executada antes de cada metodo de teste.
     * Injeta o mock do UserRepository no UserService.
     */
    @BeforeEach
    void setup() {
        mockUserRepository = mock(UserRepository.class);
        userService = new UserService(mockUserRepository);
    }

    /**
     * Testa o registro de um usuário quando o username não existe.
     * Verifica se o metodo existsByUsername é chamado e retorna false,
     * e se o metodo save é chamado e retorna true.
     */
    @Test
    @DisplayName("Deve registrar um usuário se ele não existir")
    void shouldRegisterUserIfDoesNotExist() throws SQLException {
        // simula que o usuário não existe
        when(mockUserRepository.existsByUsername(anyString())).thenReturn(false);

        // simula que o salvamento é bem-sucedido
        when(mockUserRepository.save(any(User.class))).thenReturn(true);

        boolean result = userService.registerUser("newuser", "plainpassword");
        assertThat(result).isTrue();

        // verifica se existsByUsername foi chamado para o username específico
        verify(mockUserRepository, times(1)).existsByUsername("newuser");

        // verifica se save foi chamado uma vez com qualquer objeto User
        verify(mockUserRepository, times(1)).save(any(User.class));
    }

    /**
     * Testa o registro de um usuário quando o username já existe.
     * Verifica se o metodo existsByUsername é chamado e retorna true,
     * e se o metodo save não é chamado.
     */
    @Test
    @DisplayName("Não deve registrar um usuário se ele já existir")
    void shouldNotRegisterUserIfExists() throws SQLException {
        // simula que o usuário já existe
        when(mockUserRepository.existsByUsername(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("existinguser", "plainpassword"))
            .isInstanceOf(IllegalArgumentException.class);

        // verifica se existsByUsername foi chamado uma vez
        verify(mockUserRepository, times(1)).existsByUsername("existinguser");

        // verifica que o metodo save nunca foi chamado
        verify(mockUserRepository, never()).save(any(User.class));
    }

    /**
     * Testa o login de um usuário com credenciais corretas.
     * Simula a recuperação do usuário do repositório e a verificação da senha.
     */
    @Test
    @DisplayName("Deve permitir login com credenciais corretas")
    void shouldAllowLoginWithCorrectCredentials() throws SQLException {
        String username = "testuser";
        String password = "correctpassword";

        // simula hash BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        User user = new User(1L, username, hashedPassword, "avatar.png", 0);

        // Simula que o findByUsername retorna o usuário
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> loggedInUser = userService.login(username, password);

        assertThat(loggedInUser.isPresent()).isTrue();
        assertThat(username).isEqualTo(loggedInUser.get().getUsername());
        verify(mockUserRepository, times(1)).findByUsername(username);
    }

    /**
     * Testa o login de um usuário com senha incorreta.
     * Simula a recuperação do usuário e a falha na verificação da senha.
     */
    @Test
    @DisplayName("Não deve permitir login com senha incorreta")
    void shouldNotAllowLoginWithIncorrectPassword() throws SQLException {
        String username = "testuser";
        String password = "correctpassword";
        String wrongPassword = "wrongpassword";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        User user = new User(1L, username, hashedPassword, "avatar.png", 0);

        // simula que o findByUsername retorna o usuário
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> loggedInUser = userService.login(username, wrongPassword);

        assertThat(loggedInUser.isPresent()).isFalse();
        verify(mockUserRepository, times(1)).findByUsername(username);
    }

    /**
     * Testa o login de um usuário que não existe.
     * Verifica se o findByUsername retorna Optional.empty().
     */
    @Test
    @DisplayName("Não deve permitir login para usuário inexistente")
    void shouldNotAllowLoginForNonExistentUser() throws SQLException {
        String username = "nonexistentuser";

        // simula que o findByUsername retorna um Optional vazio
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> loggedInUser = userService.login(username, "anypassword");

        assertThat(loggedInUser.isPresent()).isFalse();
        verify(mockUserRepository, times(1)).findByUsername(username);
    }

    /**
     * Testa a verificação de existência de um usuário.
     * Cobre casos onde o usuário existe (mockado como true) e não existe (mockado como false).
     */
    @Test
    @DisplayName("Deve verificar corretamente a existência do usuário")
    void shouldVerifyUserExistence() throws SQLException {
        String existingUsername = "existingUserCheck";
        String nonExistingUsername = "nonExistingUserCheck";

        when(mockUserRepository.existsByUsername(existingUsername)).thenReturn(true);
        when(mockUserRepository.existsByUsername(nonExistingUsername)).thenReturn(false);

        assertThat(userService.userExists(existingUsername)).isTrue();
        assertThat(userService.userExists(nonExistingUsername)).isFalse();

        verify(mockUserRepository, times(1)).existsByUsername(existingUsername);
        verify(mockUserRepository, times(1)).existsByUsername(nonExistingUsername);
    }

    /**
     * Testa o registro de um usuário com nome de usuário em branco.
     * Verifica se o metodo registerUser lida corretamente com nomes de usuário em branco.
     */
    @Test
    @DisplayName("Não deve permitir registro com nome de usuário em branco")
    void shouldNotAllowRegistrationWithBlankUsername() throws SQLException {
        String blankUsername = "";
        String password = "validPassword";

        // tenta registrar o usuário com nome de usuário em branco
        assertThatThrownBy(() -> userService.registerUser(blankUsername, password))
            .isInstanceOf(IllegalArgumentException.class);
        verify(mockUserRepository, never()).save(any(User.class));
    }

    /**
     * Testa o registro de um usuário com senha em branco.
     * Verifica se o metodo registerUser lida corretamente com senhas em branco.
     */
    @Test
    @DisplayName("Não deve permitir registro com senha em branco")
    void shouldNotAllowRegistrationWithBlankPassword() throws SQLException {
        String username = "blankPasswordUser";
        String blankPassword = "";

        // simula que o usuário não existe
        when(mockUserRepository.existsByUsername(username)).thenReturn(false);

        // tenta registrar o usuário com senha em branco
        assertThatThrownBy(() -> userService.registerUser(username, blankPassword))
            .isInstanceOf(IllegalArgumentException.class);
        verify(mockUserRepository, never()).save(any(User.class));
    }

    /**
     * Testa a exclusão de um usuário existente.
     * Verifica se o metodo deleteByUsername é chamado corretamente.
     */
    @Test
    @DisplayName("Deve excluir um usuário existente")
    void shouldDeleteExistingUser() throws SQLException {
        String username = "userToDelete";

        // simula que o usuário existe
        when(mockUserRepository.existsByUsername(username)).thenReturn(true);
        when(mockUserRepository.deleteByUsername(username)).thenReturn(true);

        boolean result = userService.deleteUser(username);
        assertThat(result).isTrue();

        verify(mockUserRepository, times(1)).deleteByUsername(username);
    }

    /**
     * Testa a exclusão de um usuário inexistente.
     * Verifica se o metodo deleteByUsername não é chamado quando o usuário não existe.
     */
    @Test
    @DisplayName("Não deve excluir um usuário inexistente")
    void shouldNotDeleteNonExistentUser() throws SQLException {
        String username = "nonExistentUser";

        // simula que o usuário não existe
        when(mockUserRepository.existsByUsername(username)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(username))
            .isInstanceOf(IllegalArgumentException.class);
        verify(mockUserRepository, never()).deleteByUsername(username);
    }

    /**
     * Testa a recuperação de todos os usuários.
     * Verifica se o metodo findAll é chamado e retorna uma lista de usuários.
     */
    @Test
    @DisplayName("Deve recuperar todos os usuários")
    void shouldRetrieveAllUsers() throws SQLException {
        // simula uma lista de usuários
        User user1 = new User(1L, "user1", "hashedPassword1", "avatar1.png", 0);
        User user2 = new User(2L, "user2", "hashedPassword2", "avatar2.png", 0);
        when(mockUserRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> users = userService.getAllUsers();
        assertThat(2).isEqualTo(users.size());
        assertThat(users.contains(user1)).isTrue();
        assertThat(users.contains(user2)).isTrue();

        verify(mockUserRepository, times(1)).findAll();
    }
}
