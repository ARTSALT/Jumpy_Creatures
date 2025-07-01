package com.softwaretesting.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Fábrica para criar instâncias de serviços de domínio.
 * Esta versão utiliza injeção de dependência, recebendo um {@link ConnectionProvider}
 * para construir e configurar os serviços e seus repositórios.
 * A remoção do estado estático melhora a testabilidade e a flexibilidade.
 */
public final class DatabaseFactory {

    private final ConnectionProvider provider;
    private UserService userService;

    /**
     * Constrói a fábrica com o provedor de conexão necessário.
     * A responsabilidade de criar e gerenciar o ciclo de vida do ConnectionProvider
     * é movida para fora da fábrica, seguindo o princípio da Inversão de Controle.
     *
     * @param provider O provedor de conexão a ser usado. Não pode ser nulo.
     */
    public DatabaseFactory(ConnectionProvider provider) {
        this.provider = Objects.requireNonNull(provider, "ConnectionProvider não pode ser nulo.");
    }

    /**
     * Retorna uma instância de UserService.
     * Na primeira chamada, a instância é criada e o banco de dados é inicializado.
     * Nas chamadas subsequentes, a mesma instância é retornada (padrão Singleton de escopo).
     *
     * @return Uma instância de UserService pronta para uso.
     * @throws SQLException Se ocorrer um erro ao obter a conexão ou inicializar o banco.
     */
    public UserService getUserService() throws SQLException, IOException {
        if (userService == null) {
            Connection connection = provider.getConnection();
            provider.initializeDatabase(null);

            UserRepository userRepository = new UserRepositoryImpl(connection);
            this.userService = new UserService(userRepository);
        }
        return userService;
    }

    /**
     * Fecha a conexão com o banco de dados, liberando recursos.
     * Deve ser chamado quando a fábrica não for mais necessária.
     *
     * @throws SQLException Se ocorrer um erro ao fechar a conexão.
     */
    public void close() throws SQLException {
        provider.closeConnection();
        userService = null;
    }
}
