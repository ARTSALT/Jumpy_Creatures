package com.softwaretesting.adapters.ui.view;

import com.softwaretesting.adapters.ui.dto.UserRankingDTO;

import java.util.List;

/**
 * Define o contrato para a tela de ranking de usuários.
 * Esta interface é responsável por exibir a lista de usuários ordenada por pontuação.
 */
public interface RankingView extends View {

    /**
     * Exibe a lista de usuários ordenada por pontuação em ordem crescente.
     */
    void displayRanking(List<UserRankingDTO> ranking);
}
