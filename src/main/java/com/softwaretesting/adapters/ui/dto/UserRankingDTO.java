package com.softwaretesting.adapters.ui.dto;

/**
 * Data Transfer Object (DTO) para representar o ranking de usuários.
 * Este DTO é usado para transferir informações sobre a posição, nome, avatar,
 * pontuação, número de simulações e pontuação média de cada usuário no ranking.
 */
public record UserRankingDTO(
    int position,
    String name,
    String avatarUrl,
    int score,
    int numSimulations,
    String averageScore
) {
    // construtor sem position
    public UserRankingDTO(
        String name, String avatarUrl, int score, int numSimulations, String averageScore
    ) {
        this(0, name, avatarUrl, score, numSimulations, averageScore);
    }

    // construtor que recebe o usuário atual e a posição
    public UserRankingDTO(UserRankingDTO currentUser, int position) {
        this(
            position,
            currentUser.name(),
            currentUser.avatarUrl(),
            currentUser.score(),
            currentUser.numSimulations(),
            currentUser.averageScore()
        );
    }
}
