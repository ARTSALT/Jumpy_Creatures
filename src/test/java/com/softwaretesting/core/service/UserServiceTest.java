package com.softwaretesting.core.service;

import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;
import com.softwaretesting.core.domain.port.driven.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Collections;
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
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para UserService com Mocks")
public class UserServiceTest {

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private SimulationService mockSimulationService;

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setup() {
        // Cria mocks para UserRepository e SimulationService
        userService = new UserService(mockUserRepository);
        testUser = new User("testuser", "password123");
    }

    @Nested
    @DisplayName("Testes para o método register()")
    class RegisterTests {
        @Test
        @DisplayName("Deve registrar um usuário se ele não existir")
        void shouldRegisterIfDoesNotExist() throws SQLException {
            when(mockUserRepository.existsByUsername(anyString())).thenReturn(false);
            when(mockUserRepository.save(any(User.class))).thenReturn(true);

            assertThat(userService.register(new User("newuser", "plainpassword"))).isTrue();
            verify(mockUserRepository).existsByUsername("newuser");
            verify(mockUserRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Não deve registrar um usuário se ele já existir")
        void shouldNotRegisterIfExists() throws SQLException {
            when(mockUserRepository.existsByUsername("existinguser")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(new User("existinguser", "plainpassword")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists.");
            verify(mockUserRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Não deve registrar um usuário nulo")
        void shouldNotRegisterNullUser() throws SQLException {
            assertThatThrownBy(() -> userService.register(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User cannot be null.");
            verify(mockUserRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Testes para o método login()")
    class LoginTests {
        @Test
        @DisplayName("Deve permitir login com credenciais corretas")
        void shouldAllowLoginWithCorrectCredentials() throws SQLException {
            String username = "testuser";
            String plainPassword = "correctpassword";
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
            User userInDb = new User(1L, username, hashedPassword, "avatar.png", 0);

            when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(userInDb));

            Optional<User> loggedInUser = userService.login(new User(username, plainPassword));

            assertThat(loggedInUser).isPresent();
            assertThat(loggedInUser.get().getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("Deve definir usuário como admin no login se o username for 'admin'")
        void shouldSetAdminFlagOnLoginForAdminUser() throws SQLException {
            String plainPassword = "adminpassword";
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
            User adminInDb = new User(1L, "admin", hashedPassword, "admin.png", 0);

            when(mockUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminInDb));

            Optional<User> loggedInAdmin = userService.login(new User("admin", plainPassword));

            assertThat(loggedInAdmin).isPresent();
            assertThat(loggedInAdmin.get().isAdmin()).isTrue();
        }

        @Test
        @DisplayName("Não deve permitir login com senha incorreta")
        void shouldNotAllowLoginWithIncorrectPassword() throws SQLException {
            String username = "testuser";
            String correctPassword = "correctpassword";
            String hashedPassword = BCrypt.hashpw(correctPassword, BCrypt.gensalt(10));
            User userInDb = new User(1L, username, hashedPassword, "avatar.png", 0);

            when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(userInDb));

            Optional<User> loggedInUser = userService.login(new User(username, "wrongpassword"));

            assertThat(loggedInUser).isEmpty();
        }

        @Test
        @DisplayName("Não deve permitir login para usuário inexistente")
        void shouldNotAllowLoginForNonExistentUser() throws SQLException {
            when(mockUserRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

            Optional<User> loggedInUser = userService.login(new User("nonexistentuser", "anyPassword"));

            assertThat(loggedInUser).isEmpty();
        }

        @Test
        @DisplayName("Não deve fazer login com usuário nulo")
        void shouldNotLoginWithNullUser() {
            assertThatThrownBy(() -> userService.login(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User cannot be null.");
        }
    }

    @Nested
    @DisplayName("Testes para o método userExists()")
    class UserExistsTests {
        @Test
        @DisplayName("Deve retornar true se o usuário existir")
        void shouldReturnTrueWhenUserExists() throws SQLException {
            when(mockUserRepository.existsByUsername("existingUser")).thenReturn(true);
            assertThat(userService.userExists(new User("existingUser", "p"))).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false se o usuário não existir")
        void shouldReturnFalseWhenUserDoesNotExist() throws SQLException {
            when(mockUserRepository.existsByUsername("nonExistingUser")).thenReturn(false);
            assertThat(userService.userExists(new User("nonExistingUser", "p"))).isFalse();
        }

        @Test
        @DisplayName("Não deve verificar existência com usuário nulo")
        void shouldNotVerifyExistenceWithNullUser() {
            assertThatThrownBy(() -> userService.userExists(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User cannot be null.");
        }
    }

    @Nested
    @DisplayName("Testes para o método deleteUser()")
    class DeleteUserTests {
        @Test
        @DisplayName("Deve excluir um usuário existente")
        void shouldDeleteExistingUser() throws SQLException {
            when(mockUserRepository.existsByUsername("userToDelete")).thenReturn(true);
            when(mockUserRepository.deleteByUsername("userToDelete")).thenReturn(true);

            assertThat(userService.deleteUser(new User("userToDelete", "p"))).isTrue();
            verify(mockUserRepository).deleteByUsername("userToDelete");
        }

        @Test
        @DisplayName("Não deve excluir um usuário inexistente")
        void shouldNotDeleteNonExistentUser() throws SQLException {
            when(mockUserRepository.existsByUsername("nonExistentUser")).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(new User("nonExistentUser", "p")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User does not exists.");
            verify(mockUserRepository, never()).deleteByUsername(anyString());
        }

        @Test
        @DisplayName("Não deve excluir com usuário nulo")
        void shouldNotDeleteWithNullUser() {
            assertThatThrownBy(() -> userService.deleteUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User cannot be null.");
        }
    }

    @Nested
    @DisplayName("Testes para o método getAllUsers()")
    class GetAllUsersTests {
        @Test
        @DisplayName("Deve recuperar todos os usuários e marcar o admin")
        void shouldRetrieveAllUsersAndSetAdminFlag() throws SQLException {
            User regularUser = new User(1L, "user1", "pass", "avatar1.png", 0);
            User adminUser = new User(2L, "admin", "adminpass", "avatar2.png", 0);

            when(mockUserRepository.findAll()).thenReturn(List.of(regularUser, adminUser));

            List<User> users = userService.getAllUsers();

            assertThat(users).hasSize(2);

            User foundAdmin = users.stream().filter(u -> u.getUsername().equals("admin")).findFirst().orElseThrow();
            User foundRegular = users.stream().filter(u -> u.getUsername().equals("user1")).findFirst().orElseThrow();

            assertThat(foundAdmin.isAdmin()).isTrue();
            assertThat(foundRegular.isAdmin()).isFalse();
            verify(mockUserRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Testes MC/DC para o método update()")
    class UpdateTests {
        @Test
        @DisplayName("C1=T: Deve lançar exceção se o usuário for nulo")
        void shouldThrowExceptionWhenUserIsNull() throws SQLException {
            assertThatThrownBy(() -> userService.update(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User cannot be null.");
            verify(mockUserRepository, never()).update(any(User.class));
        }

        @Test
        @DisplayName("C1=F, C2=T: Deve lançar exceção se o usuário a ser atualizado não existir")
        void shouldThrowExceptionWhenUpdatingNonExistentUser() throws SQLException {
            when(mockUserRepository.existsByUsername(testUser.getUsername())).thenReturn(false);

            assertThatThrownBy(() -> userService.update(testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User does not exist.");
            verify(mockUserRepository, never()).update(any(User.class));
        }

        @Test
        @DisplayName("C1=F, C2=F: Deve atualizar o usuário com sucesso se ele existir")
        void shouldUpdateUserSuccessfullyWhenUserExists() throws SQLException {
            when(mockUserRepository.existsByUsername(testUser.getUsername())).thenReturn(true);

            assertThatCode(() -> userService.update(testUser)).doesNotThrowAnyException();
            verify(mockUserRepository).update(testUser);
        }
    }

    @Nested
    @DisplayName("Testes MC/DC para o método updateUserScore()")
    class UpdateUserScoreTests {
        @Test
        @DisplayName("Deve definir o score como 0 se a lista de simulações bem-sucedidas for nula")
        void shouldSetScoreToZeroWhenSimulationsListIsNull() throws SQLException {
            when(mockSimulationService.getSuccessfulSimulations(testUser)).thenReturn(null);

            userService.updateUserScore(mockSimulationService, testUser);

            assertThat(testUser.getScore()).isZero();
            verify(mockUserRepository).updateScore(testUser);
        }

        @Test
        @DisplayName("Deve definir o score com base no tamanho da lista de simulações bem-sucedidas")
        void shouldSetScoreBasedOnSimulationsListSize() throws SQLException {
            List<Simulation> successfulSims = List.of(new Simulation(2, 1, 1),
                new Simulation(2, 1, 1));
            when(mockSimulationService.getSuccessfulSimulations(testUser)).thenReturn(successfulSims);

            userService.updateUserScore(mockSimulationService, testUser);

            assertThat(testUser.getScore()).isEqualTo(2);
            verify(mockUserRepository).updateScore(testUser);
        }
    }

    @Nested
    @DisplayName("Testes MC/DC para o método getUserAverageScore()")
    class GetUserAverageScoreTests {
        private final List<Simulation> nonEmptySims = List.of(
            new Simulation(2, 1, 1));
        private final List<Simulation> emptySims = Collections.emptyList();

        @Test
        @DisplayName("C1=F: Deve retornar 0.0 se a lista 'allSims' for nula")
        void shouldReturnZeroWhenAllSimsIsNull() throws SQLException {
            when(mockSimulationService.getSimulations(testUser)).thenReturn(null);

            double result = userService.getUserAverageScore(mockSimulationService, testUser);
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("C1=T, C2=F: Deve retornar 0.0 se a lista 'allSims' estiver vazia")
        void shouldReturnZeroWhenAllSimsIsEmpty() throws SQLException {
            when(mockSimulationService.getSimulations(testUser)).thenReturn(emptySims);

            double result = userService.getUserAverageScore(mockSimulationService, testUser);
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("C3=F: Deve retornar 0.0 se 'successfulSims' for nula, mas 'allSims' não")
        void shouldReturnZeroWhenSuccessfulSimsIsNull() throws SQLException {
            when(mockSimulationService.getSimulations(testUser)).thenReturn(nonEmptySims);
            when(mockSimulationService.getSuccessfulSimulations(testUser)).thenReturn(null);

            double result = userService.getUserAverageScore(mockSimulationService, testUser);
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("C4=F: Deve retornar 0.0 se 'successfulSims' for vazia, mas 'allSims' não")
        void shouldReturnZeroWhenSuccessfulSimsIsEmpty() throws SQLException {
            when(mockSimulationService.getSimulations(testUser)).thenReturn(nonEmptySims);
            when(mockSimulationService.getSuccessfulSimulations(testUser)).thenReturn(emptySims);

            double result = userService.getUserAverageScore(mockSimulationService, testUser);
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("ALL=T: Deve calcular a média corretamente quando ambas as listas são válidas")
        void shouldCalculateAverageCorrectlyWhenAllListsAreValid() throws SQLException {
            List<Simulation> allSimulations = List.of(
                new Simulation(2, 1, 1),
                new Simulation(2, 1, 1),
                new Simulation(2, 1, 1),
                new Simulation(2, 1, 1));
            List<Simulation> successfulSimulations = List.of(
                new Simulation(2, 1, 1),
                new Simulation(2, 1, 1));

            when(mockSimulationService.getSimulations(testUser)).thenReturn(allSimulations);
            when(mockSimulationService.getSuccessfulSimulations(testUser)).thenReturn(successfulSimulations);

            double result = userService.getUserAverageScore(mockSimulationService, testUser);

            assertThat(result).isEqualTo(50.0);
        }
    }
}
