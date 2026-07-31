package com.example.chookjibupuser.congestion.dto;

import java.time.OffsetDateTime;

public record BoothCongestionView(
        String congestionLevel,
        Integer waitMinutes,
        OffsetDateTime updatedAt
) {
}
