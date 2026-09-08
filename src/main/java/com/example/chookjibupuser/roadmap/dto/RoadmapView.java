// roadmap/dto/RoadmapView.java
package com.example.chookjibupuser.roadmap.dto;

import java.util.List;
import java.util.UUID;

public record RoadmapView(
        UUID roadmapPublicId,
        String mapImageUrl,
        List<ZoneView> zones,
        List<NodeView> otherNodes,
        /** 관리자가 카카오맵에 맞춰 둔 부지 경계·팜플렛. 없으면 null. */
        PresentationView presentation
) {
}