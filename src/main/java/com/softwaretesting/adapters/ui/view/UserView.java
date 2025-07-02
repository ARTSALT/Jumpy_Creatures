package com.softwaretesting.adapters.ui.view;

import com.softwaretesting.adapters.ui.dto.SimulationSummaryDTO;
import com.softwaretesting.core.domain.model.User;

import java.util.List;

/**
 * Define o contrato para a tela principal do usuário.
 * Usuário pode visualizar suas informações gerais, bem como uma lista de simulações realizadas.
 * Informações do usuário incluem nome, avatar, pontuação e média de simulações bem-sucedidas.
 * Também permite que o usuário veja detalhes de simulações específicas e altere a imagem do seu avatar.
 */
public interface UserView extends View {

    /**
     * Exibe as informações básicas do usuário na tela, como nome, avatar e pontuação.
     * Chamado pelo Presenter após o carregamento inicial dos dados.
     *
     * @param user O objeto usuário contendo as informações a serem exibidas.
     */
    void displayUserInfo(User user);

    /**
     * Preenche a lista de simulações do usuário na UI.
     *
     * @param simulations Uma lista de resumos de simulações para exibir.
     */
    void displaySimulationList(List<SimulationSummaryDTO> simulations);

    /**
     * Abre uma nova janela ou pop-up para mostrar os detalhes de uma simulação específica.
     * O Presenter será responsável por buscar os dados detalhados.
     *
     * @param simulationId O ID da simulação a ser detalhada.
     */
    void showDetailedSimulationView(int simulationId);
}
