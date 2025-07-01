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

    // mock do UserRepository para simular interações com o banco de dados
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

        assertThat(userService.registerUser(new User("newuser", "plainpassword"))).isTrue();

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

        assertThatThrownBy(() -> userService.registerUser(new User("existinguser", "plainpassword")))
            .isInstanceOf(IllegalArgumentException.class);

        // verifica se existsByUsername foi chamado uma vez
        verify(mockUserRepository, times(1)).existsByUsername("existinguser");

        // verifica que o metodo save nunca foi chamado
        verify(mockUserRepository, never()).save(any(User.class));
    }

    // Testes MC/DC registerUser
    @Test
    @DisplayName("MC/DC if (user == null)")
    void shouldNotRegisterNullUser() throws SQLException {
        // simula que o usuário é nulo
        assertThatThrownBy(() -> userService.registerUser(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User cannot be null.");

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
        String plainPassword = "correctpassword";

        // cria um usuário com nome e senha em texto plano
        User loginAttemptUser = new User(username, plainPassword);

        // simula que o findByUsername retorna um usuário com senha hasheada
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        User userInDb = new User(1L, username, hashedPassword, "avatar.png", 0);
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(userInDb));

        // tenta fazer o login com o usuário que tem a senha em texto plano
        Optional<User> loggedInUser = userService.login(loginAttemptUser);

        assertThat(loggedInUser).isPresent();
        assertThat(loggedInUser.get().getUsername()).isEqualTo(username);
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
        String correctPassword = "correctpassword";
        String wrongPassword = "wrongpassword";

        // cria um usuário com nome e senha incorreta
        User loginAttemptUser = new User(username, wrongPassword);

        // simula que o findByUsername retorna um usuário com senha hasheada
        String hashedPassword = BCrypt.hashpw(correctPassword, BCrypt.gensalt(10));
        User userInDb = new User(1L, username, hashedPassword, "avatar.png", 0);
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(userInDb));

        // tenta fazer o login com a senha incorreta
        Optional<User> loggedInUser = userService.login(loginAttemptUser);

        assertThat(loggedInUser).isEmpty();
        verify(mockUserRepository, times(1)).findByUsername(username);
    }

    /**
     * Testa o login de um usuário que não existe.
     * Verifica se o findByUsername retorna Optional.empty().
     */
    @Test
    @DisplayName("Não deve permitir login para usuário inexistente")
    void shouldNotAllowLoginForNonExistentUser() throws SQLException {
        User user = new User("nonexistentuser", "anyPassword");

        // simula que o findByUsername retorna um Optional vazio
        when(mockUserRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        Optional<User> loggedInUser = userService.login(user);

        assertThat(loggedInUser.isPresent()).isFalse();
        verify(mockUserRepository, times(1)).findByUsername(user.getUsername());
    }

    @Test
    @DisplayName("MC/DC if (user == null) no login")
    void shouldNotLoginWithNullUser() throws SQLException {
        // simula que o usuário é nulo
        assertThatThrownBy(() -> userService.login(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User cannot be null.");

        // verifica que o metodo findByUsername nunca foi chamado
        verify(mockUserRepository, never()).findByUsername(anyString());
    }

    /**
     * Testa a verificação de existência de um usuário.
     * Cobre casos onde o usuário existe (mockado como true) e não existe (mockado como false).
     */
    @Test
    @DisplayName("Deve verificar corretamente a existência do usuário")
    void shouldVerifyUserExistence() throws SQLException {
        User existingUser = new User("existingUsername", "anyPassword");
        User nonExistingUser = new User("nonExistingUsername", "anyPassword");

        when(mockUserRepository.existsByUsername(existingUser.getUsername())).thenReturn(true);
        when(mockUserRepository.existsByUsername(nonExistingUser.getUsername())).thenReturn(false);

        assertThat(userService.userExists(existingUser)).isTrue();
        assertThat(userService.userExists(nonExistingUser)).isFalse();

        verify(mockUserRepository, times(1)).existsByUsername(existingUser.getUsername());
        verify(mockUserRepository, times(1)).existsByUsername(nonExistingUser.getUsername());
    }

    @Test
    @DisplayName("MC/DC if (user == null) na verificação de existência do usuário")
    void shouldNotVerifyExistenceWithNullUser() throws SQLException {
        // simula que o usuário é nulo
        assertThatThrownBy(() -> userService.userExists(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User cannot be null.");

        // verifica que o metodo existsByUsername nunca foi chamado
        verify(mockUserRepository, never()).existsByUsername(anyString());
    }

    /**
     * Testa a exclusão de um usuário existente.
     * Verifica se o metodo deleteByUsername é chamado corretamente.
     */
    @Test
    @DisplayName("Deve excluir um usuário existente")
    void shouldDeleteExistingUser() throws SQLException {
        User userToDelete = new User("userToDelete", "anyPassword");

        // simula que o usuário existe
        when(mockUserRepository.existsByUsername(userToDelete.getUsername())).thenReturn(true);
        when(mockUserRepository.deleteByUsername(userToDelete.getUsername())).thenReturn(true);

        assertThat(userService.deleteUser(userToDelete)).isTrue();

        verify(mockUserRepository, times(1)).deleteByUsername(userToDelete.getUsername());
    }

    /**
     * Testa a exclusão de um usuário inexistente.
     * Verifica se o metodo deleteByUsername não é chamado quando o usuário não existe.
     */
    @Test
    @DisplayName("Não deve excluir um usuário inexistente")
    void shouldNotDeleteNonExistentUser() throws SQLException {
        User user = new User("nonExistentUser", "anyPassword");

        // simula que o usuário não existe
        when(mockUserRepository.existsByUsername(user.getUsername())).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(user))
            .isInstanceOf(IllegalArgumentException.class);
        verify(mockUserRepository, never()).deleteByUsername(user.getUsername());
    }

    @Test
    @DisplayName("MC/DC if (user == null) na exclusão de usuário")
    void shouldNotDeleteWithNullUser() throws SQLException {
        // simula que o usuário é nulo
        assertThatThrownBy(() -> userService.deleteUser(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User cannot be null.");

        // verifica que o metodo deleteByUsername nunca foi chamado
        verify(mockUserRepository, never()).deleteByUsername(anyString());
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
