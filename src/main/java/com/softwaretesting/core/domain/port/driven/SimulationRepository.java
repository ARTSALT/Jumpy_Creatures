package com.softwaretesting.core.domain.port.driven;

import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface para o repositório de simulações, definindo métodos para operações CRUD.
 * Simulações são 'constantes', isso significa que uma vez criadas, não são alteradas, apenas salvas ou excluídas.
 * Uma simulação é criada por um usuário e associada a ele.
 */
public interface SimulationRepository {
    Simulation save(Simulation simulation) throws SQLException;
    boolean delete(Simulation simulation) throws SQLException;
    List<Simulation> findAllByUser(User user) throws SQLException;
    List<Simulation> findAll() throws SQLException;
}
