package com.softwaretesting.adapters.ui.dto;

public record UserStatisticsDTO(
    String username,
    int score,
    int simulationsRun
) {}
