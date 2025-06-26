package com.softwaretesting.simulation;

/**
 * Interface para fornecer números aleatórios.
 * Implementações desta interface devem fornecer um metodo para gerar um número aleatório
 * num intervalo especificado.
 */
public interface RandomProvider {
    double nextDouble(double min, double max);
}
