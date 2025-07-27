package com.softwaretesting.adapters.ui.presenter;

import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.screen.AdminScreen;
import com.softwaretesting.adapters.ui.screen.LoginScreen;
import com.softwaretesting.adapters.ui.screen.RankingScreen;
import com.softwaretesting.adapters.ui.view.AdminView;
import com.softwaretesting.core.application.service.SimulationService;
import com.softwaretesting.core.domain.model.Simulation;
import com.softwaretesting.core.domain.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminPresenter {

    private final AdminView adminView;
    private final LibGdxApplication application;

    public AdminPresenter(AdminScreen adminScreen, LibGdxApplication application) {
        this.adminView = adminScreen;
        this.application = application;
    }

    public void onAdminScreenLoaded() {
        try {
            List<User> users = application.getDatabaseFactory().getUserService().getAllUsers();
            if (!users.isEmpty()) adminView.showUsersList(users.stream()
                    .filter(user -> !user.isAdmin()) // remove o admin da lista
                    .toList());
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error fetching users list", e);
        }

        try {
            List<Simulation> simulations = application.getDatabaseFactory().getSimulationService().getAllSimulations();
            if (!simulations.isEmpty()) adminView.showSimulationsList(simulations);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching simulations list", e);
        }
    }

    public void onRankingButtonClicked() {
        application.navigateTo(new RankingScreen(application));
    }

    public void onDeleteUserClicked(User user) {
        try {
            application.getDatabaseFactory().getUserService().deleteUser(user);
            adminView.removeUserFromList(user.getId());
            var simulations = application.getDatabaseFactory().getSimulationService().getAllSimulations();
            adminView.showSimulationsList(simulations);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error on deleting user", e);
        }
    }

    public void onDeleteSimulationClicked(Long id) {
        try {
            SimulationService simulationService = application.getDatabaseFactory().getSimulationService();
            User userS = simulationService.getSimulationUser(id);
            simulationService.delete(id);
            application.getDatabaseFactory().getUserService().updateUserScore(simulationService, userS);
            adminView.removeSimulationFromList(id);
            adminView.showUsersList(
                application.getDatabaseFactory().getUserService().getAllUsers()
                    .stream()
                    .filter(user -> !user.isAdmin()) // remove o admin da lista
                    .toList()
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error on deleting simulation", e);
        } catch (IOException e) {
            throw new RuntimeException("Error updating user score", e);
        }
    }

    public void onUserLogout() {
        application.setCurrentUser(null);
        application.navigateTo(new LoginScreen(application));
    }
}
