package com.softwaretesting;

import com.softwaretesting.libgdx.Lwjgl3Launcher;

public class Main {
    public static void main(String[] args) {
        try {
            Lwjgl3Launcher.launch();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println("Erro na execução da aplicação: " + e.getMessage());
        }
    }
}
