package com.softwaretesting.adapters.persistence;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Report;
import net.jqwik.api.Reporting;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Testes para H2ConnectionProvider")
class H2ConnectionProviderTest {

    // mocks para simular dependências externas (dublês de teste)
    private Connection mockConnection;
    private Statement mockStatement;

    private MockedStatic<DriverManager> driverManagerMockedStatic;

    @BeforeEach
    void setUp() {
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);

        // mock para o metodo estático DriverManager.getConnection()
        driverManagerMockedStatic = mockStatic(DriverManager.class);
    }

    @AfterEach
    void tearDown() {
        // fecha o mock estático para não interferir em outros testes
        driverManagerMockedStatic.close();
    }

    // =========================================================================================
    // TESTES DE DOMÍNIO E FRONTEIRA - Builder
    // =========================================================================================
    @Nested
    @DisplayName("Testes do Builder")
    class BuilderTests {

        @Test
        @DisplayName("Deve construir com valores padrão para banco de dados persistente")
        void buildWithDefaultValues() {
            H2ConnectionProvider provider = H2ConnectionProvider.builder().build();

            assertThat(provider)
                    .extracting("url", "user", "password", "scriptFile")
                    // CORREÇÃO: A URL padrão agora é outra, devido à nova lógica do builder.
                    .containsExactly("jdbc:h2:./data/usersdb;DB_CLOSE_DELAY=-1", "sa", "", "database/schema.sql");
        }

        @Test
        @DisplayName("Deve construir com banco em memória quando useInMemory(true) é chamado")
        void buildWithUseInMemoryTrue() {
            // CORREÇÃO: Adicionado .database("testdb") para que a URL seja construída
            // com o nome de banco de dados esperado pelo teste.
            H2ConnectionProvider provider = H2ConnectionProvider.builder().database("testdb").useInMemory(true).build();

            assertThat(provider)
                    .extracting("url")
                    .isEqualTo("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        }

        @Test
        @DisplayName("Deve construir com banco persistente quando useInMemory(false) é chamado")
        void buildWithUseInMemoryFalse() {
            // CORREÇÃO: Adicionado .database("testdb") e corrigida a URL esperada.
            H2ConnectionProvider provider = H2ConnectionProvider.builder().database("testdb").useInMemory(false).build();

            assertThat(provider)
                    .extracting("url")
                    .isEqualTo("jdbc:h2:./data/testdb;DB_CLOSE_DELAY=-1");
        }

        @Test
        @DisplayName("Deve sobrescrever valores padrão com dados customizados")
        void buildWithCustomValues() {
            H2ConnectionProvider provider = H2ConnectionProvider.builder()
                    .url("jdbc:h2:mem:customdb")
                    .user("testuser")
                    .password("testpass")
                    .scriptFile("custom/script.sql")
                    .build();

            assertThat(provider)
                    .extracting("url", "user", "password", "scriptFile")
                    .containsExactly("jdbc:h2:mem:customdb", "testuser", "testpass", "custom/script.sql");
        }

        // MC/DC para a condição: if (url == null || url.isEmpty()) no build()
        @Test
        @DisplayName("[MC/DC] Deve usar URL padrão quando a URL explícita é nula")
        void buildWhenUrlIsNull() {
            // Caso 1: C1=true, C2=X -> Resultado=true
            H2ConnectionProvider provider = H2ConnectionProvider.builder().url(null).build();
            // CORREÇÃO: A URL padrão foi alterada.
            assertThat(provider).extracting("url").isEqualTo("jdbc:h2:./data/usersdb;DB_CLOSE_DELAY=-1");
        }

        @Test
        @DisplayName("[MC/DC] Deve usar URL padrão quando a URL explícita é vazia")
        void build_whenUrlIsEmpty() {
            // Caso 2: C1=false, C2=true -> Resultado=true
            H2ConnectionProvider provider = H2ConnectionProvider.builder().url("").build();
            // CORREÇÃO: A URL padrão foi alterada.
            assertThat(provider).extracting("url").isEqualTo("jdbc:h2:./data/usersdb;DB_CLOSE_DELAY=-1");
        }

        @Test
        @DisplayName("[MC/DC] Deve usar URL fornecida quando ela não é nula nem vazia")
        void buildWhenUrlIsNotEmpty() {
            // Caso 3: C1=false, C2=false -> Resultado=false
            String customUrl = "jdbc:h2:./my_custom_db";
            H2ConnectionProvider provider = H2ConnectionProvider.builder().url(customUrl).build();
            assertThat(provider).extracting("url").isEqualTo(customUrl);
        }
    }

    // =========================================================================================
    // TESTES DE DOMÍNIO E ESTRUTURAL (MC/DC) - Ciclo de Vida da Conexão
    // =========================================================================================
    @Nested
    @DisplayName("Testes do Ciclo de Vida da Conexão")
    class ConnectionLifecycleTests {

        // MC/DC para a condição: if (connection == null || connection.isClosed())
        @Test
        @DisplayName("[MC/DC] getConnection deve criar uma nova conexão se ela for nula")
        void whenConnectionIsNullShouldCreateNewConnection() throws SQLException {
            // Caso 1: C1=true, C2=X -> Resultado=true
            H2ConnectionProvider provider = H2ConnectionProvider.builder().build();
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            Connection conn = provider.getConnection();

            assertThat(conn).isSameAs(mockConnection);
            // CORREÇÃO: A URL padrão usada pelo builder() foi atualizada.
            driverManagerMockedStatic.verify(
                    () -> DriverManager.getConnection("jdbc:h2:./data/usersdb;DB_CLOSE_DELAY=-1", "sa", ""),
                    times(1));
        }

        @Test
        @DisplayName("[MC/DC] getConnection deve criar uma nova conexão se a existente estiver fechada")
        void whenConnectionIsClosed() throws SQLException {
            // Caso 2: C1=false, C2=true -> Resultado=true
            H2ConnectionProvider provider = H2ConnectionProvider.builder().build();
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // primeira chamada cria a conexão
            provider.getConnection();

            // simula que a conexão foi fechada
            when(mockConnection.isClosed()).thenReturn(true);

            // segunda chamada deve criar uma nova conexão
            provider.getConnection();

            driverManagerMockedStatic.verify(
                    () -> DriverManager.getConnection(anyString(), anyString(), anyString()),
                    times(2));
        }

        @Test
        @DisplayName("[MC/DC] getConnection deve retornar a conexão existente se ela estiver aberta")
        void whenConnectionIsOpen() throws SQLException {
            // Caso 3: C1=false, C2=false -> Resultado=false
            H2ConnectionProvider provider = H2ConnectionProvider.builder().build();
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // primeira chamada
            provider.getConnection();
            // simula que a conexão está aberta
            when(mockConnection.isClosed()).thenReturn(false);
            // segunda chamada
            Connection conn2 = provider.getConnection();

            assertThat(conn2).isSameAs(mockConnection);
            driverManagerMockedStatic.verify(
                    () -> DriverManager.getConnection(anyString(), anyString(), anyString()),
                    times(1));
        }

        // MC/DC para a condição: if (connection == null || connection.isClosed()) no metodo closeConnection()
        @Test
        @DisplayName("[MC/DC] closeConnection deve fechar a conexão se ela estiver aberta")
        void closeConnectionWhenOpen() throws SQLException {
            // Caso 1: C1=true, C2=X -> Resultado=true
            H2ConnectionProvider provider = H2ConnectionProvider.builder().build();
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            provider.getConnection();
            when(mockConnection.isClosed()).thenReturn(false);

            provider.closeConnection();

            verify(mockConnection).close();
        }

        @Test
        @DisplayName("[MC/DC] closeConnection não deve fechar a conexão se ela já estiver fechada")
        void closeConnectionWhenClosed() throws SQLException {
            // Caso 2: C1=false, C2=true -> Resultado=true
            H2ConnectionProvider provider = H2ConnectionProvider.builder().build();
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            provider.getConnection();
            when(mockConnection.isClosed()).thenReturn(true);

            provider.closeConnection();

            verify(mockConnection, never()).close();
        }

        @Test
        @DisplayName("[MC/DC] closeConnection não deve fazer nada se a conexão for nula")
        void closeConnectionWhenNull() throws SQLException {
            // Caso 3: C1=false, C2=false -> Resultado=false
            H2ConnectionProvider provider = H2ConnectionProvider.builder().build();

            provider.closeConnection();

            verify(mockConnection, never()).close();
        }

        @Test
        @DisplayName("Deve lançar SQLException se ocorrer um erro ao fechar a conexão")
        void closeConnectionThrowsSQLException() throws SQLException {
            H2ConnectionProvider provider = H2ConnectionProvider.builder().build();
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            provider.getConnection();
            when(mockConnection.isClosed()).thenReturn(false);
            doThrow(new SQLException("Erro ao fechar a conexão com o banco de dados.")).when(mockConnection).close();

            assertThatThrownBy(provider::closeConnection)
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("Erro ao fechar a conexão com o banco de dados.")
                    .hasCauseInstanceOf(SQLException.class);
        }
    }

    // =========================================================================================
    // TESTES DE DOMÍNIO, FRONTEIRA E ESTRUTURAL (MC/DC) - Inicialização do Banco
    // =========================================================================================
    @Nested
    @DisplayName("Testes de Inicialização do Banco de Dados")
    class DatabaseInitializationTests {

        private H2ConnectionProvider provider;

        // MC/DC para a condição: if (inputStream == null)
        @Test
        @DisplayName("[MC/DC] initializeDatabase deve usar script padrão se InputStream for nulo")
        void whenInputStreamIsNull() throws SQLException, IOException {
            // Caso 1: inputStream == null
            provider = H2ConnectionProvider.builder().scriptFile("database/schema.sql").build();

            // mock do DriverManager para retornar a conexão
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // mock da conexão para retornar o statement
            when(mockConnection.createStatement()).thenReturn(mockStatement);

            provider.getConnection();
            provider.initializeDatabase(null);

            verify(mockConnection, times(3)).createStatement();
            verify(mockStatement).execute("""
                // cria tabela de usuários caso não exista
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    avatar_url VARCHAR(255) DEFAULT 'images/default_avatar.png',
                    score INT DEFAULT 0
                )""");
            verify(mockStatement).execute("""
                // insere usuário admin padrão se não existir
                INSERT INTO users (username, password)
                SELECT 'admin', '$2a$10$9OGJDg8B8zKVKaAsWUhJZu0.aBi1rxcfeKyJC/38gxT8rI44jozoq'
                WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')""");
            verify(mockStatement).execute("""
                // cria a tabela de simulações
                CREATE TABLE IF NOT EXISTS simulations (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    num_creatures INT NOT NULL,
                    iterations INT NOT NULL,
                    is_successful BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )""");
        }

        @Test
        @DisplayName("[MC/DC] Deve inicializar o banco de dados a partir de um script SQL fornecido")
        void initializeDatabaseSuccessful() throws SQLException, IOException {
            // Caso 2: inputStream != null
            provider = H2ConnectionProvider.builder().build();

            // configura o mock do DriverManager para retornar a conexão
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // configura o mock da conexão para retornar o statement
            when(mockConnection.createStatement()).thenReturn(mockStatement);

            provider.getConnection();
            String sqlContent = "CREATE TABLE temp_users (id INT);;INSERT INTO temp_users VALUES (42);";
            InputStream sqlStream = new ByteArrayInputStream(sqlContent.getBytes(StandardCharsets.UTF_8));

            provider.initializeDatabase(sqlStream);

            verify(mockConnection, times(2)).createStatement();
            verify(mockStatement).execute("CREATE TABLE temp_users (id INT)");
            verify(mockStatement).execute("INSERT INTO temp_users VALUES (42)");
        }

        @Test
        @DisplayName("Deve lançar SQLException se o script SQL padrão não for encontrado")
        void whenScriptFileNotFound() throws SQLException {
            provider = H2ConnectionProvider.builder().scriptFile("nonexistent.sql").build();

            // mock do DriverManager para retornar a conexão
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);

            provider.getConnection();

            assertThatThrownBy(() -> provider.initializeDatabase(null))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("Script SQL não encontrado: nonexistent.sql");
        }

        @Test
        @DisplayName("Deve lançar SQLException se houver erro na execução de um comando SQL")
        void whenStatementFails() throws SQLException {
            provider = H2ConnectionProvider.builder().build();

            // mock do DriverManager para retornar a conexão
            driverManagerMockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);

            // simula uma falha no primeiro comando
            doThrow(new SQLException("Erro ao executar os comandos SQL.")).when(mockStatement).execute(anyString());

            provider.getConnection();
            assertThatThrownBy(() -> provider.initializeDatabase(null))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("Erro ao executar os comandos SQL.")
                    .hasCauseInstanceOf(SQLException.class);
        }
    }

    // =========================================================================================
    // TESTES BASEADOS EM PROPRIEDADES
    // =========================================================================================
    @Nested
    @DisplayName("Testes Baseados em Propriedades")
    class PropertyBasedTests {

        @Property
        @DisplayName("O builder deve sempre criar uma instância válida")
        @Report(Reporting.GENERATED)
        void builderShouldAlwaysCreateValidInstance(
                @ForAll @AlphaChars @StringLength(min = 1, max = 50) String url,
                @ForAll @AlphaChars @StringLength(min = 1, max = 20) String user,
                @ForAll @AlphaChars @StringLength(min = 1, max = 20) String password,
                @ForAll @AlphaChars @StringLength(min = 1, max = 50) String scriptFile
        ) {
            // para qualquer conjunto de strings não nulas/vazias, o builder deve configurar corretamente o objeto.
            H2ConnectionProvider provider = H2ConnectionProvider.builder()
                    .url(url)
                    .user(user)
                    .password(password)
                    .scriptFile(scriptFile)
                    .build();

            assertThat(provider).isNotNull();
            assertThat(provider)
                    .extracting("url", "user", "password", "scriptFile")
                    .containsExactly(url, user, password, scriptFile);
        }
    }
}