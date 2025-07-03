package com.softwaretesting.adapters.persistence;

import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;
import com.softwaretesting.core.domain.port.driven.SimulationRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class SimulationRepositoryImpl implements SimulationRepository {

    private final Connection connectionProvider;

    public SimulationRepositoryImpl(Connection connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Simulation save(Simulation simulation) throws SQLException {
        String sql = "INSERT INTO simulations (user_id, name, num_creatures, iterations) " +
            "VALUES (?, ?, ?, ?) RETURNING id, created_at";
        try (var ps = connectionProvider.prepareStatement(sql)) {
            ps.setLong(1, simulation.getUser().getId());
            ps.setString(2, simulation.getName());
            ps.setInt(3, simulation.getCreatures().size());
            ps.setInt(4, simulation.getIterations());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to insert simulation, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    Timestamp createdAt = generatedKeys.getTimestamp(2);

                    simulation.setId(id);
                    simulation.setCreatedAt(createdAt.toLocalDateTime());

                    return simulation;
                } else {
                    throw new SQLException("Creating simulation failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public boolean delete(Simulation simulation) throws SQLException {
        String sql = "DELETE FROM simulations WHERE id = ?";
        try (var ps = connectionProvider.prepareStatement(sql)) {
            ps.setLong(1, simulation.getId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new SQLException("Failed to delete simulation with ID: " + simulation.getId(), e);
        }
    }

    @Override
    public List<Simulation> findAllByUser(User user) throws SQLException {
        String sql = "SELECT * FROM simulations WHERE user_id = ?";
        try (var ps = connectionProvider.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<Simulation> simulations = new java.util.ArrayList<>();
                while (rs.next()) {
                    Simulation newSimulation = new Simulation(
                        rs.getInt("num_creatures"),
                        rs.getInt("iterations"),
                        rs.getInt("horizon_width")
                    );
                    newSimulation.setId(rs.getLong("id"));
                    newSimulation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    newSimulation.setUser(user);
                    simulations.add(newSimulation);
                }
                return simulations;
            }
        }
        catch (SQLException e) {
            throw new SQLException("Failed to retrieve simulations for user with ID: " + user.getId(), e);
        }
    }

    @Override
    public List<Simulation> findAll() throws SQLException {
        String sql = "SELECT * FROM simulations";
        try (var ps = connectionProvider.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Simulation> simulations = new java.util.ArrayList<>();
            while (rs.next()) {
                Simulation simulation = new Simulation(
                    rs.getInt("num_creatures"),
                    rs.getInt("iterations"),
                    rs.getInt("horizon_width")
                );
                simulation.setId(rs.getLong("id"));
                simulation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                simulations.add(simulation);
            }
            return simulations;
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve all simulations", e);
        }
    }
}
