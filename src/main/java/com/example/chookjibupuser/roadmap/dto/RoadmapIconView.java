package com.example.chookjibupuser.roadmap.dto;

import java.math.BigDecimal;

public record RoadmapIconView(
        Long placementId,
        String iconCode,
        String iconName,
        String iconImageUrl,
        Long relatedBoothId,
        BigDecimal positionX,
        BigDecimal positionY,
        BigDecimal rotationDeg,
        String label
) {
}
