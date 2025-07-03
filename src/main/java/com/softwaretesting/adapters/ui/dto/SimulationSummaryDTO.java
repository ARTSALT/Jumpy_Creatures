package com.softwaretesting.adapters.ui.dto;

public record SimulationSummaryDTO(
    Long id,
    String name,
    String date,
    int zombies,
    int iterations,
    boolean wasSuccessful
) {}
