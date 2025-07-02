package com.softwaretesting.core.application.service;

import com.softwaretesting.core.domain.model.RandomProvider;
import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.port.driven.SimulationRepository;

import java.util.List;

public class SimulationService {

    public SimulationService(SimulationRepository simulationRepository) {

    }

     public void createSimulation(Simulation simulation) {

     }

     public List<Simulation> getAllSimulations() {
        RandomProvider randomProvider = (min, max) -> Math.random() * (max - min) + min;
         return List.of(
             new Simulation(1, 2, 500, randomProvider),
             new Simulation(2, 5, 120, randomProvider),
             new Simulation(3, 10, 250, randomProvider)
         );
     }

     public void updateSimulation(Simulation simulation) {

     }

     public void deleteSimulation(int simulationId) {

     }
}
