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
) {}
