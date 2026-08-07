package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.roadmap.dto.RoadmapView;

import java.util.List;

public record RoadmapResponse(
        String roadmapType,
        String baseImageUrl,
        Integer canvasWidth,
        Integer canvasHeight,
        List<RoadmapIconResponse> icons
) {

    public static RoadmapResponse from(RoadmapView view) {
        return new RoadmapResponse(
                view.roadmapType(),
                view.baseImageUrl(),
                view.canvasWidth(),
                view.canvasHeight(),
                view.icons().stream().map(RoadmapIconResponse::from).toList()
        );
    }
}
