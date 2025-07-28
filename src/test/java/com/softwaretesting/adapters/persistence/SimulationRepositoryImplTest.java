package com.softwaretesting.adapters.persistence;

import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Testes para SimulationRepositoryImpl")
class SimulationRepositoryImplTest {

    private static H2ConnectionProvider connectionProvider;
    private Connection connection;
    private SimulationRepositoryImpl simulationRepository;
    private User testUser;
    private User anotherUser;

    @BeforeAll
    static void setupDatabase() throws SQLException, IOException {
        // Inicializa o banco de dados em memória uma vez para todos os testes
        connectionProvider = H2ConnectionProvider.builder().useInMemory(true).build();
        connectionProvider.getConnection();
        connectionProvider.initializeDatabase(null);
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Obtém uma nova conexão e cria os repositórios para cada teste
        connection = connectionProvider.getConnection();
        simulationRepository = new SimulationRepositoryImpl(connection);
        UserRepositoryImpl userRepository = new UserRepositoryImpl(connection);

        // Cria usuários de teste para garantir a integridade referencial
        testUser = new User("testuser", "pass123");
        userRepository.save(testUser);
        testUser = userRepository.findByUsername("testuser").orElseThrow();

        anotherUser = new User("anotheruser", "pass456");
        userRepository.save(anotherUser);
        anotherUser = userRepository.findByUsername("anotheruser").orElseThrow();
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Limpa as tabelas após cada teste para garantir o isolamento
        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM simulations");
            st.execute("DELETE FROM users WHERE username IN ('testuser', 'anotheruser')");
        }
    }

    @Nested
    @DisplayName("Método save")
    class SaveTests {
        @Test
        @DisplayName("Deve salvar uma simulação e retornar o objeto com ID e data de criação")
        void shouldSaveSimulationAndReturnWithIdAndTimestamp() throws SQLException {
            Simulation sim = new Simulation(5, 10, 1000);
            sim.setName("Primeiro Teste");
            sim.setUser(testUser);
            sim.setSuccessful(true);

            simulationRepository.save(sim);

            assertThat(sim.getId()).isNotNull().isPositive();
            assertThat(sim.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar SQLException ao tentar salvar uma simulação com usuário nulo")
        void shouldThrowExceptionWhenSavingWithNullUser() {
            Simulation sim = new Simulation(5, 10, 1000);
            sim.setName("Simulação Órfã");
            // sim.setUser(testUser); // Usuário não é definido

            assertThatThrownBy(() -> simulationRepository.save(sim))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Método delete")
    class DeleteTests {
        @Test
        @DisplayName("Deve deletar uma simulação existente pelo seu ID")
        void shouldDeleteExistingSimulation() throws SQLException {
            Simulation sim = new Simulation(2, 5, 1000);
            sim.setName("Para Deletar");
            sim.setUser(testUser);
            simulationRepository.save(sim);

            assertThat(sim.getId()).isNotNull();

            simulationRepository.delete(sim.getId());

            List<Simulation> userSims = simulationRepository.findAllByUser(testUser);
            assertThat(userSims).isEmpty();
        }
    }

    @Nested
    @DisplayName("Métodos de Busca")
    class FindTests {
        @BeforeEach
        void setupSimulations() throws SQLException {
            // Cria um cenário com múltiplas simulações para testar as buscas
            Simulation s1 = new Simulation(10, 20, 1000);
            s1.setName("Sim A de TestUser");
            s1.setUser(testUser);
            s1.setIterations(20);
            simulationRepository.save(s1);

            Simulation s2 = new Simulation(5, 15, 1000);
            s2.setName("Sim B de TestUser");
            s2.setUser(testUser);
            s2.setIterations(15);
            simulationRepository.save(s2);

            Simulation s3 = new Simulation(7, 7, 1000);
            s3.setName("Sim C de AnotherUser");
            s3.setUser(anotherUser);
            s3.setIterations(7);
            simulationRepository.save(s3);
        }

        @Test
        @DisplayName("findAllByUser deve retornar apenas as simulações do usuário especificado")
        void shouldFindAllSimulationsForSpecificUser() throws SQLException {
            List<Simulation> testUserSims = simulationRepository.findAllByUser(testUser);
            List<Simulation> anotherUserSims = simulationRepository.findAllByUser(anotherUser);

            assertThat(testUserSims).hasSize(2);
            assertThat(testUserSims).extracting(Simulation::getName).containsExactlyInAnyOrder(
                "Sim A de TestUser", "Sim B de TestUser");
            assertThat(anotherUserSims).hasSize(1);
            assertThat(anotherUserSims.getFirst().getName()).isEqualTo("Sim C de AnotherUser");
        }

        @Test
        @DisplayName("findAll deve retornar todas as simulações de todos os usuários")
        void shouldFindAllSimulationsFromAllUsers() throws SQLException {
            List<Simulation> allSims = simulationRepository.findAll();
            assertThat(allSims).hasSize(3);
        }

        @Test
        @DisplayName("findUserBySimulationId deve retornar o usuário correto")
        void shouldFindUserBySimulationId() throws SQLException {
            List<Simulation> allSims = simulationRepository.findAll();
            Simulation simC = allSims.stream().filter(s -> s.getName()
                .equals("Sim C de AnotherUser")).findFirst().orElseThrow();

            User foundUser = simulationRepository.findUserBySimulationId(simC.getId());

            assertThat(foundUser.getId()).isEqualTo(anotherUser.getId());
            assertThat(foundUser.getUsername()).isEqualTo(anotherUser.getUsername());
        }

        @Test
        @DisplayName("findUserBySimulationId deve lançar exceção para ID inexistente")
        void shouldThrowExceptionWhenFindingUserForNonExistentSimulation() {
            assertThatThrownBy(() -> simulationRepository.findUserBySimulationId(9999L))
                .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("findAll deve retornar lista vazia quando não há simulações")
        void shouldReturnEmptyListWhenNoSimulations() throws SQLException {
            // Limpa as simulações antes do teste
            try (Statement st = connection.createStatement()) {
                st.execute("DELETE FROM simulations");
            }
            List<Simulation> simulations = simulationRepository.findAll();
            assertThat(simulations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Método countByUserId")
    class CountTests {
        @Test
        @DisplayName("Deve contar corretamente o número de simulações de um usuário")
        void shouldCorrectlyCountSimulationsForUser() throws SQLException {
            Simulation s1 = new Simulation(10, 20, 1000);
            s1.setUser(testUser);
            s1.setName("Simulação 1");
            simulationRepository.save(s1);

            Simulation s2 = new Simulation(5, 15, 1000);
            s2.setUser(testUser);
            s2.setName("Simulação 2");
            simulationRepository.save(s2);

            int count = simulationRepository.countByUserId(testUser.getId());
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve retornar 0 para um usuário sem simulações")
        void shouldReturnZeroForUserWithNoSimulations() throws SQLException {
            int count = simulationRepository.countByUserId(testUser.getId());
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionTests {

        private Connection mockConnection;
        private SimulationRepositoryImpl repositoryWithMockConnection;
        private java.sql.PreparedStatement mockPreparedStatement;
        private java.sql.ResultSet mockResultSet;


        @BeforeEach
        void setupMocks() throws SQLException {
            // Usa mocks para simular falhas no banco de dados
            mockConnection = mock(Connection.class);
            mockPreparedStatement = mock(java.sql.PreparedStatement.class);
            mockResultSet = mock(java.sql.ResultSet.class);
            repositoryWithMockConnection = new SimulationRepositoryImpl(mockConnection);

            // Configuração padrão de sucesso para a maioria dos testes
            when(mockConnection.prepareStatement(anyString(), any(String[].class))).thenReturn(mockPreparedStatement);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        }

        @Test
        @DisplayName("save deve lançar SQLException se prepareStatement falhar")
        void saveShouldThrowSQLExceptionOnPrepareStatementFailure() throws SQLException {
            when(mockConnection.prepareStatement(anyString(), any(String[].class)))
                .thenThrow(new SQLException("DB connection lost"));
            Simulation sim = new Simulation(2, 1, 1);
            sim.setUser(testUser);
            assertThatThrownBy(() -> repositoryWithMockConnection.save(sim))
                .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("delete deve lançar SQLException se prepareStatement falhar")
        void deleteShouldThrowSQLExceptionOnPrepareStatementFailure() throws SQLException {
            when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB connection lost"));
            assertThatThrownBy(() -> repositoryWithMockConnection.delete(99L))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("Failed to delete simulation with ID: 99");
        }

        @Test
        @DisplayName("findAllByUser deve lançar SQLException se prepareStatement falhar")
        void findAllByUserShouldThrowSQLExceptionOnPrepareStatementFailure() throws SQLException {
            when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB connection lost"));
            assertThatThrownBy(() -> repositoryWithMockConnection.findAllByUser(testUser))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("Failed to retrieve simulations for user with ID: " + testUser.getId());
        }

        @Test
        @DisplayName("findAll deve lançar SQLException se prepareStatement falhar")
        void findAllShouldThrowSQLExceptionOnPrepareStatementFailure() throws SQLException {
            when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB connection lost"));
            assertThatThrownBy(() -> repositoryWithMockConnection.findAll())
                .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("findUserBySimulationId deve lançar SQLException se prepareStatement falhar")
        void findUserBySimulationIdShouldThrowSQLExceptionOnPrepareStatementFailure() throws SQLException {
            when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB connection lost"));
            assertThatThrownBy(() -> repositoryWithMockConnection.findUserBySimulationId(1L))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("Failed to find user by simulation ID: 1");
        }

        @Test
        @DisplayName("countByUserId deve lançar SQLException se prepareStatement falhar")
        void countByUserIdShouldThrowSQLExceptionOnPrepareStatementFailure() throws SQLException {
            when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB connection lost"));
            assertThatThrownBy(() -> repositoryWithMockConnection.countByUserId(1L))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("Error counting simulations for user ID: 1");
        }

        @Test
        @DisplayName("findAll deve lançar SQLException se executeQuery falhar")
        void findAllShouldThrowSQLExceptionOnExecuteQueryFailure() throws SQLException {
            when(mockPreparedStatement.executeQuery())
                .thenThrow(new SQLException("Query execution failed"));
            assertThatThrownBy(() -> repositoryWithMockConnection.findAll())
                .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("findAll deve lançar SQLException se rs.next() falhar")
        void findAllShouldThrowSQLExceptionOnResultSetNextFailure() throws SQLException {
            // Simula que o ResultSet existe, mas lança um erro ao tentar iterar sobre ele.
            when(mockResultSet.next()).thenThrow(new SQLException("Failed to read next row"));

            assertThatThrownBy(() -> repositoryWithMockConnection.findAll())
                .isInstanceOf(SQLException.class)
                .hasMessage("Failed to retrieve all simulations");
        }
    }
}
