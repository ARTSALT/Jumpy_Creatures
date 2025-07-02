package com.softwaretesting.adapters.ui.view;

import com.softwaretesting.adapters.ui.dto.UserStatisticsDTO;
import java.util.List;

/**
 * Define o contrato para a tela de estatísticas da simulação.
 */
public interface StatisticsView {

    /**
     * Exibe a lista de estatísticas individuais de cada usuário.
     *
     * @param userStats A lista de dados estatísticos por usuário.
     */
    void displayUserStatistics(List<UserStatisticsDTO> userStats);

    /**
     * Exibe as estatísticas totais e de média do sistema.
     *
     * @param totalSimulations A quantidade total de simulações executadas por todos.
     * @param avgSuccessPerUser A média de simulações bem-sucedidas por usuário.
     * @param overallAvgSuccess A média total de simulações bem-sucedidas.
     */
    void displayOverallStatistics(int totalSimulations, float avgSuccessPerUser, float overallAvgSuccess);

    /**
     * Instrui a View a navegar de volta para a UserScreen.
     */
    void navigateBack();
}
