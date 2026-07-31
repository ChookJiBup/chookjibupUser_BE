package com.example.chookjibupuser.roadmap.dto;

import java.util.List;

public record RoadmapView(
        String roadmapType,
        String baseImageUrl,
        Integer canvasWidth,
        Integer canvasHeight,
        List<RoadmapIconView> icons
) {
}
