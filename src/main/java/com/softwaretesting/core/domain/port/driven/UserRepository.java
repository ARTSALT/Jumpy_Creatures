package com.softwaretesting.core.domain.port.driven;

import com.softwaretesting.core.domain.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interface para o repositório de usuários, definindo métodos para operações CRUD.
 * Implementações específicas devem fornecer a lógica de acesso ao banco de dados.
 */
public interface UserRepository {
    boolean save(User user) throws SQLException;
    void update(User user) throws SQLException;
    void updateScore(User user) throws SQLException;
    boolean deleteByUsername(String username) throws SQLException;
    Optional<User> findByUsername(String username) throws SQLException;
    boolean existsByUsername(String username) throws SQLException;
    List<User> findAll() throws SQLException;
}
