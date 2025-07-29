package com.softwaretesting.adapters.ui.view;

/**
 * Interface que define o contrato para as views da aplicação.
 * As views são responsáveis por exibir informações ao usuário e receber interações.
 * Cada view deve implementar os métodos definidos aqui para exibir mensagens e liberar recursos.
 */
public interface View {

    // define os tipos de mensagens que podem ser exibidas na tela
    enum MessageType {
        INFO,
        ERROR,
        WARNING,
        SUCCESS
    }

    /**
     * Exibe uma mensagem na tela.
     *
     * @param message A mensagem a ser exibida.
     * @param type O tipo da mensagem, que pode ser INFO, ERROR, WARNING ou SUCCESS.
     */
    void showMessage(String message, MessageType type);
}
