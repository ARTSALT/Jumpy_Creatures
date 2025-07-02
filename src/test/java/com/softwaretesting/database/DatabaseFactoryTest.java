package com.softwaretesting.database;

import com.softwaretesting.adapters.persistence.ConnectionProvider;
import com.softwaretesting.adapters.persistence.DatabaseFactory;
import com.softwaretesting.core.application.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}
