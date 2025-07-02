package com.softwaretesting.adapters.ui.view;

/**
 * Interface que define o contrato para a tela de login.
 * Possui campos para capturar o nome de usuário e senha, realizar o login e cadastrar um novo usuário.
 */
public interface LoginView extends View {
    String getUsername();
    String getPassword();
}
