package com.softwaretesting;

import com.softwaretesting.simulation.RandomProvider;
import com.softwaretesting.simulation.Simulation;

public class Main {

    public static void main(String[] args) {
        int numeroDeCriaturas = 5;
        double larguraDaCriatura = 350.0;
        int larguraDoHorizonte = 1000;
        int maximoDeIteracoes = 100;
        RandomProvider randomProvider = (min, max) -> Math.random() * (max - min) + min;

        Simulation simulacao = new Simulation(
            numeroDeCriaturas, larguraDaCriatura, larguraDoHorizonte,
            maximoDeIteracoes, randomProvider
        );

        while (simulacao.prepareNextIteration()) {
            simulacao.executeNextIteration();
        }

        System.out.println("\n==================================");
        System.out.println("FIM DA SIMULAÇÃO");
        if (simulacao.isSuccessful()) {
            System.out.println("Resultado: SUCESSO!");
        } else {
            System.out.println("Resultado: Limite de iterações atingido.");
        }
        System.out.printf("Total de Iterações: %d\n", simulacao.getCurrentIteration());
        System.out.println("==================================");
    }
}
