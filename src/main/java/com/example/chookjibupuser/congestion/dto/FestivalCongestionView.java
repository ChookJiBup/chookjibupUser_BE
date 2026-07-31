package com.example.chookjibupuser.congestion.dto;

import java.time.OffsetDateTime;

public record FestivalCongestionView(
        String congestionLevel,
        OffsetDateTime updatedAt
) {
}
