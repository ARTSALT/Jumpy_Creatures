package com.softwaretesting.adapters.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Fornece uma conexão com o banco de dados H2, permitindo a inicialização do banco de dados
 * a partir de um script SQL e o gerenciamento da conexão.
 * Esta classe implementa a interface ConnectionProvider.
 */
public class H2ConnectionProvider implements ConnectionProvider {

	private Connection connection;	 // conexão com o banco de dados H2
	private final String url;		 // URL de conexão com o banco de dados
	private final String user;		 // usuário para autenticação
	private final String password;	 // senha para autenticação
	private final String scriptFile; // caminho do arquivo de script SQL para inicialização do banco de dados

    private H2ConnectionProvider(Builder builder) {
		this.url = builder.url;
		this.user = builder.user;
		this.password = builder.password;
		this.scriptFile = builder.scriptFile;
    }

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String url;
		private String database;
		private String user = "sa";
		private String password = "";
		private String scriptFile = "database/schema.sql";

        public Builder useInMemory(boolean useInMemory) {
            if (useInMemory) {
				this.url = "jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1";
			} else {
				this.url = "jdbc:h2:./data/" + database + ";DB_CLOSE_DELAY=-1";
			}
			return this;
		}

		public Builder url(String url) {
			this.url = url;
            return this;
		}

		public Builder database(String database) {
			this.database = database;
			return this;
		}

		public Builder user(String user) {
			this.user = user;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}

		public Builder scriptFile(String scriptFile) {
			this.scriptFile = scriptFile;
			return this;
		}

		public H2ConnectionProvider build() {
			if (database == null) {
				database = "usersdb"; // nome padrão do banco de dados
			}
			if (url == null || url.isEmpty()) {
				useInMemory(false);	// se a URL não for especificada, usa o modo em arquivo por padrão
			}
			return new H2ConnectionProvider(this);
		}
	}

	/**
	 * Obtém uma conexão com o banco de dados H2.
	 * @return Connection
	 * @throws SQLException se ocorrer um erro ao estabelecer a conexão
	 */
	@Override
	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(url, user, password);
		}
		return connection;
	}

	/**
	 * Inicializa o banco de dados H2 executando um script SQL.
	 * O script deve estar localizado no classpath.
	 * @param inputStream InputStream do script SQL
	 * @throws SQLException se ocorrer um erro ao executar o script
	 */
	@Override
	public void initializeDatabase(InputStream inputStream) throws SQLException, IOException {
		// carrega o conteúdo do script SQL padrão se nenhum InputStream for fornecido
		if (inputStream == null) {
			try (InputStream is = getClass().getClassLoader().getResourceAsStream(scriptFile)) {
				if (is == null) {
					throw new SQLException("Script SQL não encontrado: " + scriptFile);
				}
				executeCommands(is);
			}
        }
		else {
			// executa o script SQL fornecido
			executeCommands(inputStream);
		}
	}

	/**
	 * Executa os comandos SQL contidos no InputStream.
	 * Cada comando deve ser separado por ponto e vírgula.
	 * @param inputStream InputStream contendo os comandos SQL
	 * @throws SQLException se ocorrer um erro ao executar os comandos
	 */
	private void executeCommands(InputStream inputStream) throws SQLException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String sql = reader.lines().collect(Collectors.joining("\n"));
			for (String command : sql.split(";")) {
				if (!(command = command.trim()).isEmpty()) {
					try (Statement statement = connection.createStatement()) {
						statement.execute(command);
					}
				}
			}
		} catch (SQLException | IOException e) {
			throw new SQLException("Erro ao executar os comandos SQL.", e);
		}
	}

	/**
	 * Fecha a conexão com o banco de dados H2, se estiver aberta.
	 */
	@Override
	public void closeConnection() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new SQLException("Erro ao fechar a conexão com o banco de dados.", e);
			}
			connection = null;
		}
	}
}
