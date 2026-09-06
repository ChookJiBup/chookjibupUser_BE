// api/festival/dto/RoadmapNodeResponse.java (신규)
package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.roadmap.GeometryType;
import com.example.chookjibupuser.roadmap.NodeType;
import com.example.chookjibupuser.roadmap.dto.NodeView;
import java.util.UUID;

public record RoadmapNodeResponse(
        UUID publicId, NodeType nodeType, String name,
        GeometryType geometryType, String geometryData, int sortOrder
) {
    public static RoadmapNodeResponse from(NodeView view) {
        return new RoadmapNodeResponse(view.publicId(), view.nodeType(), view.name(),
                view.geometryType(), view.geometryData(), view.sortOrder());
    }
}