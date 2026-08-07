package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.roadmap.dto.RoadmapIconView;

import java.math.BigDecimal;

public record RoadmapIconResponse(
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

    public static RoadmapIconResponse from(RoadmapIconView view) {
        return new RoadmapIconResponse(
                view.placementId(),
                view.iconCode(),
                view.iconName(),
                view.iconImageUrl(),
                view.relatedBoothId(),
                view.positionX(),
                view.positionY(),
                view.rotationDeg(),
                view.label()
        );
    }
}
