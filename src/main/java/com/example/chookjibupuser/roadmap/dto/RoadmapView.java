// roadmap/dto/RoadmapView.java
package com.example.chookjibupuser.roadmap.dto;

import java.util.List;
import java.util.UUID;

public record RoadmapView(UUID roadmapPublicId, String mapImageUrl, List<ZoneView> zones, List<NodeView> otherNodes) {
}