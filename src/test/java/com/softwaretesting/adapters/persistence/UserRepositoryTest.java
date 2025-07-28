package com.softwaretesting.adapters.persistence;

import com.softwaretesting.core.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Testes para UserRepositoryImpl")
class UserRepositoryTest {

    private PreparedStatement mockPs;
    private ResultSet mockRs;

    private UserRepositoryImpl userRepository;

    private User testUser;

    @BeforeEach
    void setUp() throws SQLException {
        // mocks para simular a infraestrutura JDBC
        Connection mockConnection = mock(Connection.class);
        mockPs = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);
        userRepository = new UserRepositoryImpl(mockConnection);

        // configuração padrão: sempre que um PreparedStatement for criado, retorna o mock
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);

        // objeto de usuário padrão para os testes
        testUser = new User(1L, "user", "password123", "avatar.png", 100);
    }

    @Nested
    @DisplayName("Testes para o método save()")
    class SaveTests {

        @Test
        @DisplayName("Deve retornar true ao salvar um usuário com sucesso")
        void userSuccessfulSave() throws SQLException {
            when(mockPs.executeUpdate()).thenReturn(1);

            boolean result = userRepository.save(testUser);

            assertThat(result).isTrue();

            // verifica se os dados do usuário foram corretamente passados para o PreparedStatement
            verify(mockPs).setString(1, "user");
            verify(mockPs).setString(2, "password123");
            // avatar.png não existe, então o padrão é usado
            verify(mockPs).setString(3, "images/default_avatar.png");
            verify(mockPs).setString(4, "100");
        }

        @Test
        @DisplayName("Deve retornar false se nenhuma linha for afetada")
        void whenRowsNotAffected() throws SQLException {
            when(mockPs.executeUpdate()).thenReturn(0);

            boolean result = userRepository.save(testUser);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Deve propagar SQLException em caso de erro no banco")
        void whenDbErrorOccurs() throws SQLException {
            when(mockPs.executeUpdate()).thenThrow(new SQLException("Erro de inserção"));

            assertThatThrownBy(() -> userRepository.save(testUser))
                .isInstanceOf(SQLException.class)
                .hasMessage("Erro de inserção");
        }
    }

    @Nested
    @DisplayName("Testes para o método deleteByUsername()")
    class DeleteByUsernameTests {

        @Test
        @DisplayName("Deve retornar true ao deletar um usuário com sucesso")
        void userSuccessfulDelete() throws SQLException {
            when(mockPs.executeUpdate()).thenReturn(1);

            boolean result = userRepository.deleteByUsername("user");

            assertThat(result).isTrue();
            verify(mockPs).setString(1, "user");
        }

        @Test
        @DisplayName("Deve retornar false se nenhuma linha for afetada")
        void whenRowsNotAffected() throws SQLException {
            when(mockPs.executeUpdate()).thenReturn(0);

            boolean result = userRepository.deleteByUsername("nonexistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Deve propagar SQLException em caso de erro no banco")
        void whenDbErrorOccurs() throws SQLException {
            when(mockPs.executeUpdate()).thenThrow(new SQLException("Erro de deleção"));

            assertThatThrownBy(() -> userRepository.deleteByUsername("user"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Erro de deleção");
        }
    }

    @Nested
    @DisplayName("Testes para os métodos de atualização (update)")
    class UpdateTests {

        @Test
        @DisplayName("update() deve chamar executeUpdate com os parâmetros corretos")
        void updateShouldCallExecuteUpdateWithCorrectParameters() throws SQLException {
            // Ação
            userRepository.update(testUser);

            // Verificação: Garante que os dados corretos foram passados para o PreparedStatement
            verify(mockPs).setString(1, testUser.getPassword());
            verify(mockPs).setString(2, testUser.getAvatarUrl());
            verify(mockPs).setInt(3, testUser.getScore());
            verify(mockPs).setString(4, testUser.getUsername());

            // Verifica se a operação de atualização foi de fato executada
            verify(mockPs, times(1)).executeUpdate();
        }

        @Test
        @DisplayName("update() deve propagar SQLException em caso de erro no banco")
        void updateShouldPropagateSqlExceptionOnDbError() throws SQLException {
            // Configura o mock para lançar uma exceção ao executar o update
            when(mockPs.executeUpdate()).thenThrow(new SQLException("Erro de atualização"));

            // Ação e Verificação
            assertThatThrownBy(() -> userRepository.update(testUser))
                .isInstanceOf(SQLException.class)
                .hasMessage("Erro de atualização");
        }

        @Test
        @DisplayName("updateScore() deve chamar executeUpdate com os parâmetros corretos")
        void updateScoreShouldCallExecuteUpdateWithCorrectParameters() throws SQLException {
            // Ação
            userRepository.updateScore(testUser);

            // Verificação
            verify(mockPs).setInt(1, testUser.getScore());
            verify(mockPs).setString(2, testUser.getUsername());
            verify(mockPs, times(1)).executeUpdate();
        }

        @Test
        @DisplayName("updateScore() deve propagar SQLException em caso de erro no banco")
        void updateScoreShouldPropagateSqlExceptionOnDbError() throws SQLException {
            // Configura o mock para lançar uma exceção
            when(mockPs.executeUpdate()).thenThrow(new SQLException("Erro de atualização de score"));

            // Ação e Verificação
            assertThatThrownBy(() -> userRepository.updateScore(testUser))
                .isInstanceOf(SQLException.class)
                .hasMessage("Erro de atualização de score");
        }
    }

    @Nested
    @DisplayName("Testes para o método findByUsername()")
    class FindByUsernameTests {

        @Test
        @DisplayName("Deve retornar um Optional com o usuário se ele for encontrado")
        void whenUserExistsReturnValidOptional() throws SQLException {
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getLong("id")).thenReturn(testUser.getId());
            when(mockRs.getString("username")).thenReturn(testUser.getUsername());
            when(mockRs.getString("password")).thenReturn(testUser.getPassword());
            when(mockRs.getString("avatar_url")).thenReturn(testUser.getAvatarUrl());
            when(mockRs.getInt("score")).thenReturn(testUser.getScore());

            Optional<User> foundUser = userRepository.findByUsername("user");

            assertThat(foundUser).isPresent();
            assertThat(foundUser.get()).usingRecursiveComparison().isEqualTo(testUser);
            verify(mockPs).setString(1, "user");
        }

        @Test
        @DisplayName("Deve retornar um Optional vazio se o usuário não for encontrado")
        void whenUserNotExistsReturnEmptyOptional() throws SQLException {
            // simula um ResultSet sem resultados
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false); // nenhum registro encontrado

            Optional<User> foundUser = userRepository.findByUsername("nonexistent");

            assertThat(foundUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes para o método existsByUsername()")
    class ExistsByUsernameTests {

        @Test
        @DisplayName("Deve retornar true se o usuário existir")
        void whenUserExists() throws SQLException {
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getInt(1)).thenReturn(1);

            boolean exists = userRepository.existsByUsername("user");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false se o usuário não existir")
        void whenUserDoesNotExist() throws SQLException {
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getInt(1)).thenReturn(0);

            boolean exists = userRepository.existsByUsername("nonexistent");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes para o método findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Deve retornar uma lista com todos os usuários")
        void shouldReturnListOfUsers() throws SQLException {
            User user2 = new User(2L, "user2", "pass", "avatar.png", 50);
            when(mockPs.executeQuery()).thenReturn(mockRs);

            // simula a iteração: primeira chamada retorna true, segunda true, terceira false
            when(mockRs.next()).thenReturn(true).thenReturn(true).thenReturn(false);

            // configura os retornos para o primeiro usuário
            when(mockRs.getLong("id")).thenReturn(testUser.getId())
                .thenReturn(user2.getId());

            when(mockRs.getString("username")).thenReturn(testUser.getUsername())
                .thenReturn(user2.getUsername());

            when(mockRs.getString("password")).thenReturn(testUser.getPassword())
                .thenReturn(user2.getPassword());

            when(mockRs.getString("avatar_url")).thenReturn(testUser.getAvatarUrl())
                .thenReturn(user2.getAvatarUrl());

            when(mockRs.getInt("score")).thenReturn(testUser.getScore())
                .thenReturn(user2.getScore());

            List<User> users = userRepository.findAll();

            assertThat(users).hasSize(2);
            assertThat(users.get(0)).usingRecursiveComparison().isEqualTo(testUser);
            assertThat(users.get(1)).usingRecursiveComparison().isEqualTo(user2);
        }

        @Test
        @DisplayName("Deve retornar uma lista vazia se não houver usuários")
        void shouldReturnEmptyList() throws SQLException {
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            List<User> users = userRepository.findAll();

            assertThat(users).isNotNull().isEmpty();
        }
    }
}
