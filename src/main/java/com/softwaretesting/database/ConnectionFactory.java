package com.softwaretesting.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Classe responsável por criar conexões com o banco de dados H2.
 * Pode ser configurada para usar um banco de dados em memória ou um arquivo no disco.
 * Também é responsável por inicializar o esquema do banco de dados a partir de um script SQL.
 */
public abstract class ConnectionFactory {

	private static final String JDBC_URL_FILE = "jdbc:h2:./data/usersdb;DB_CLOSE_DELAY=-1"; // arquivo H2 no disco
	private static final String JDBC_URL_MEM = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"; // banco de dados em memória
	private static final String USER = "sa";
	private static final String PASSWORD = "sah2adminpassword";
	private static final String SCRIPT_FILE = "database/schema.sql"; // arquivo SQL com o esquema do bd

	// retorna uma conexão com o banco de dados H2, pode ser em memória ou arquivo
	public static Connection getConnection(boolean useInMemory) throws SQLException {
		String URL = useInMemory ? JDBC_URL_MEM : JDBC_URL_FILE;
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	// inicializa o esquema e os dados do banco de dados
	public static void initializeDatabase(Connection connection) throws SQLException {
		try (InputStream is = ConnectionFactory.class.getClassLoader().getResourceAsStream(SCRIPT_FILE);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))) {

			// lê o arquivo de script e executa os comandos SQL
			String sql = reader.lines().collect(Collectors.joining("\n"));
			for (String command : sql.split(";")) {
				if (!command.trim().isEmpty()) {
					try (Statement statement = connection.createStatement()) {
						statement.execute(command);
					}
				}
			}
		} catch (Exception e) {
			throw new SQLException("Erro ao ler ou executar o script SQL " + SCRIPT_FILE + ": " + e.getMessage(), e);
		}
	}
}
