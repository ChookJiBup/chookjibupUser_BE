package com.example.chookjibupuser.festival.dto;

import java.util.List;

public record FestivalPageView(
        List<FestivalSummaryView> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
