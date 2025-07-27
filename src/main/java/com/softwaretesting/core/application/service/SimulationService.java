package com.softwaretesting.core.application.service;

import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;
import com.softwaretesting.core.domain.port.driven.SimulationRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Serviço para gerenciar simulações, incluindo criação, exclusão e recuperação de simulações.
 * Utiliza SimulationRepository para interações com o banco de dados.
 */
public class SimulationService {

    private final SimulationRepository simulationRepository;

    public SimulationService(SimulationRepository simulationRepository) {
        this.simulationRepository = simulationRepository;
    }

    public void register(Simulation simulation) throws SQLException {
        if (simulation == null) {
            throw new IllegalArgumentException("Simulation cannot be null.");
        }
        if (simulation.getUser() == null) {
            throw new IllegalArgumentException("Simulation must be associated with a user.");
        }

        simulationRepository.save(simulation);
    }

    public void delete(Long id) throws SQLException {
        if (id == null || id < 0) {
            throw new IllegalArgumentException("ID cannot be null or negative.");
        }

        simulationRepository.delete(id);
    }

    public List<Simulation> getSimulations(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        return simulationRepository.findAllByUser(user);
    }

    public List<Simulation> getAllSimulations() throws SQLException {
        return simulationRepository.findAll();
    }

    public List<Simulation> getSuccessfulSimulations(User user) throws SQLException {
        return getSimulations(user)
            .stream()
            .filter(Simulation::isSuccessful)
            .toList();
    }
}
