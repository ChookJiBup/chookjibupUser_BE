package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.congestion.dto.BoothCongestionView;
import java.time.OffsetDateTime;

public record BoothCongestionResponse(
        String congestionLevel,
        Integer waitMinutes,
        OffsetDateTime updatedAt
) {

    public static BoothCongestionResponse from(BoothCongestionView view) {
        return new BoothCongestionResponse(view.congestionLevel(), view.waitMinutes(), view.updatedAt());
    }
}
