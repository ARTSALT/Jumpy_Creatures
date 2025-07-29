package com.softwaretesting.adapters.persistence;

import com.softwaretesting.core.service.SimulationService;
import com.softwaretesting.core.service.UserService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Testes para DatabaseFactory")
class DatabaseFactoryTest {

    // dublês de teste (mocks) para isolar a classe DatabaseFactory
    private ConnectionProvider mockProvider;
    private Connection mockConnection;

    private DatabaseFactory factory;

    @BeforeEach
    void setUp() {
        mockProvider = mock(ConnectionProvider.class);
        mockConnection = mock(Connection.class);

        // a fábrica é instanciada antes de cada teste com seu mock
        factory = new DatabaseFactory(mockProvider);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Reseta o mock após cada teste para evitar efeitos colaterais
        reset(mockProvider, mockConnection);
        factory.close();
    }

    @Nested
    @DisplayName("Testes do Construtor")
    class ConstructorTests {

        @Test
        @DisplayName("Deve lançar NullPointerException se o ConnectionProvider for nulo")
        void whenProviderIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> new DatabaseFactory(null))
                .withMessage("ConnectionProvider não pode ser nulo.");
        }

        @Test
        @DisplayName("Deve criar uma instância com sucesso se o ConnectionProvider for válido")
        void whenProviderIsValid() {
            assertThatCode(() -> new DatabaseFactory(mockProvider)).doesNotThrowAnyException();
            assertThat(new DatabaseFactory(mockProvider)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Testes de Criação do UserService")
    class UserServiceCreationTests {

        @BeforeEach
        void setupMocks() throws SQLException {
            // configuração padrão do mock para os testes
            when(mockProvider.getConnection()).thenReturn(mockConnection);
        }

        @Test
        @DisplayName("Primeira chamada a getUserService deve inicializar o DB e criar o serviço")
        void firstCallInitializesDatabase() throws SQLException, IOException {
            UserService userService = factory.getUserService();
            assertThat(userService).isNotNull();

            // verifica se os métodos corretos foram chamados na ordem esperada
            verify(mockProvider, times(1)).getConnection();
            verify(mockProvider, times(1)).initializeDatabase(null);
        }

        @Test
        @DisplayName("Múltiplas chamadas a getUserService devem retornar a mesma instância singleton")
        void shouldReturnSameInstance() throws SQLException, IOException {
            UserService userService1 = factory.getUserService();
            UserService userService2 = factory.getUserService();

            assertThat(userService1).isNotNull();
            assertThat(userService1).isSameAs(userService2); // verifica se é a mesma instância

            // verifica que a inicialização ocorreu apenas uma vez
            verify(mockProvider, times(1)).getConnection();
            verify(mockProvider, times(1)).initializeDatabase(null);
        }

        @Test
        @DisplayName("Deve propagar SQLException se getConnection falhar")
        void shouldThrowSQLExceptionWhenGetConnectionFails() throws SQLException {
            when(mockProvider.getConnection()).thenThrow(new SQLException());

            assertThatThrownBy(() -> factory.getUserService())
                .isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("Deve propagar SQLException se initializeDatabase falhar")
        void whenInitializeDatabaseFails() throws SQLException, IOException {
            doThrow(new SQLException()).when(mockProvider).initializeDatabase(null);

            assertThatThrownBy(() -> factory.getUserService())
                .isInstanceOf(SQLException.class);
        }
    }

    @Nested
    @DisplayName("Testes de Criação do SimulationService")
    class SimulationServiceCreationTests {

        @BeforeEach
        void setupMocks() throws SQLException {
            // Configura o mock para retornar uma conexão válida por padrão
            when(mockProvider.getConnection()).thenReturn(mockConnection);
        }

        @Test
        @DisplayName("Deve criar o serviço com sucesso se o UserService já estiver inicializado")
        void shouldCreateServiceSuccessfullyWhenUserServiceExists() throws SQLException, IOException {
            // Pré-condição: Garante que o UserService seja criado primeiro
            factory.getUserService();

            // Ação
            SimulationService simulationService = factory.getSimulationService();

            // Verificação
            assertThat(simulationService).isNotNull();
            // Verifica se getConnection foi chamado duas vezes no total (uma para cada serviço)
            verify(mockProvider, times(2)).getConnection();
        }

        @Test
        @DisplayName("Múltiplas chamadas devem retornar a mesma instância singleton")
        void shouldReturnSameInstanceOnMultipleCalls() throws SQLException, IOException {
            // Pré-condição: Garante que ambos os serviços sejam criados
            factory.getUserService();
            SimulationService service1 = factory.getSimulationService();

            // Ação
            SimulationService service2 = factory.getSimulationService();

            // Verificação
            assertThat(service1).isNotNull();
            assertThat(service1).isSameAs(service2); // Verifica se é a mesma instância

            // Verifica que getConnection foi chamado apenas uma vez para cada serviço,
            // provando que a inicialização não ocorreu de novo
            verify(mockProvider, times(2)).getConnection();
        }

        @Test
        @DisplayName("Deve lançar IllegalStateException se o UserService não for inicializado primeiro")
        void shouldThrowIllegalStateExceptionIfUserServiceIsNull() {
            // Ação e Verificação: Tenta criar o SimulationService sem antes criar o UserService
            assertThatThrownBy(() -> factory.getSimulationService())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("UserService deve ser inicializado antes de SimulationService.");
        }

        @Test
        @DisplayName("Deve propagar SQLException se getConnection falhar na sua criação")
        void shouldPropagateSQLExceptionWhenConnectionFails() throws SQLException, IOException {
            // Pré-condição: Garante que o UserService foi criado com sucesso
            factory.getUserService();

            // Prepara o mock para falhar na PRÓXIMA chamada a getConnection.
            when(mockProvider.getConnection()).thenThrow(new SQLException("Database connection failed"));

            // Ação e Verificação: Tenta criar o SimulationService, esperando que a exceção seja propagada
            assertThatThrownBy(() -> factory.getSimulationService())
                .isInstanceOf(SQLException.class)
                .hasMessage("Database connection failed");
        }
    }
}
