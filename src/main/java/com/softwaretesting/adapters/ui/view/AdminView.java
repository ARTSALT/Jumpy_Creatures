package com.softwaretesting.adapters.ui.view;

import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;

import java.util.List;

public interface AdminView extends View {
    void showUsersList(List<User> users);
    void removeUserFromList(Long userId);
    void showSimulationsList(List<Simulation> simulations);
    void removeSimulationFromList(Long simulationId);
}
