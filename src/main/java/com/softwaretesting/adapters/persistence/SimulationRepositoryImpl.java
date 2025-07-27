package com.softwaretesting.adapters.persistence;

import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;
import com.softwaretesting.core.domain.port.driven.SimulationRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do repositório de simulações, utilizando JDBC para persistência em banco de dados.
 * Esta classe fornece métodos para salvar, deletar e buscar simulações associadas a um usuário.
 */
public class SimulationRepositoryImpl implements SimulationRepository {

    private final Connection connectionProvider;

    /**
     * Construtor que recebe um provedor de conexão para interagir com o banco de dados.
     * @param connectionProvider Provedor de conexão JDBC.
     */
    public SimulationRepositoryImpl(Connection connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void save(Simulation simulation) throws SQLException {
        String sql = "INSERT INTO simulations (user_id, name, num_creatures, iterations, is_successful) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (var ps = connectionProvider.prepareStatement(sql, new String[]{"id", "created_at"})) {
            ps.setLong(1, simulation.getUser().getId());
            ps.setString(2, simulation.getName());
            ps.setInt(3, simulation.getInitialNumCreatures());
            ps.setInt(4, simulation.getIterations());
            ps.setBoolean(5, simulation.isSuccessful());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to insert simulation, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong("id");
                    Timestamp createdAt = generatedKeys.getTimestamp("created_at");

                    simulation.setId(id);
                    simulation.setCreatedAt(createdAt.toLocalDateTime());

                } else {
                    throw new SQLException("Creating simulation failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM simulations WHERE id = ?";
        try (var ps = connectionProvider.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Failed to delete simulation with ID: " + id, e);
        }
    }

    @Override
    public List<Simulation> findAllByUser(User user) throws SQLException {
        List<Simulation> simulations = new ArrayList<>();
        String sql = "SELECT id, name, num_creatures, iterations, is_successful, created_at "
                   + "FROM simulations WHERE user_id = ?";
        try (var ps = connectionProvider.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String name = rs.getString("name");
                    int numCreatures = rs.getInt("num_creatures");
                    int iterations = rs.getInt("iterations");
                    boolean isSuccessful = rs.getBoolean("is_successful");
                    Timestamp createdAt = rs.getTimestamp("created_at");

                    Simulation simulation = new Simulation(numCreatures, iterations, 1000);
                    simulation.setId(id);
                    simulation.setName(name);
                    simulation.setIterations(iterations);
                    simulation.setSuccessful(isSuccessful);
                    simulation.setCreatedAt(createdAt.toLocalDateTime());
                    simulation.setUser(user);
                    simulations.add(simulation);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve simulations for user with ID: " + user.getId(), e);
        }
        return simulations;
    }

    @Override
    public List<Simulation> findAll() throws SQLException {
        String sql = "SELECT s.id, s.user_id, s.name, s.num_creatures, s.iterations, s.is_successful, s.created_at, u.username " +
            "FROM simulations s " +
            "JOIN users u ON s.user_id = u.id";

        try (var ps = connectionProvider.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Simulation> simulations = new ArrayList<>();
            while (rs.next()) {
                int numCreatures = rs.getInt("num_creatures");
                int iterations = rs.getInt("iterations");
                boolean isSuccessful = rs.getBoolean("is_successful");

                Simulation simulation = new Simulation(numCreatures, iterations, 1000);
                simulation.setId(rs.getLong("id"));
                simulation.setName(rs.getString("name"));
                simulation.setIterations(iterations);
                simulation.setSuccessful(isSuccessful);
                simulation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                User user = new User(rs.getLong("user_id"), rs.getString("username"));
                simulation.setUser(user);

                simulations.add(simulation);
            }
            return simulations;
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve all simulations", e);
        }
    }

    @Override
    public User findUserBySimulationId(Long id) throws SQLException {
        String sql = "SELECT u.id, u.username FROM simulations s " +
                     "JOIN users u ON s.user_id = u.id WHERE s.id = ?";

        try (var ps = connectionProvider.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long userId = rs.getLong("id");
                    String username = rs.getString("username");
                    return new User(userId, username);
                } else {
                    throw new SQLException("No user found for simulation ID: " + id);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to find user by simulation ID: " + id, e);
        }
    }

    @Override
    public int countByUserId(Long id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM simulations WHERE user_id = ?";
        try (var ps = connectionProvider.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to count simulations for user ID: " + id);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error counting simulations for user ID: " + id, e);
        }
    }
}
