package com.softwaretesting.adapters.ui.dto;

public record UserRankingDTO(
    int position,
    String name,
    int score
) {
    /**
     * Construtor para criar um UserRankingDTO a partir de um nome e uma pontuação.
     * A posição é definida como -1, indicando que não foi especificada.
     *
     * @param name  O nome do usuário.
     * @param score A pontuação do usuário.
     */
    public UserRankingDTO(String name, int score) {
        this(-1, name, score);
    }
}
