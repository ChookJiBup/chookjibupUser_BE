// roadmap/dto/NodeView.java
package com.example.chookjibupuser.roadmap.dto;

import com.example.chookjibupuser.roadmap.GeometryType;
import com.example.chookjibupuser.roadmap.NodeType;
import com.example.chookjibupuser.roadmap.RoadmapNode;
import java.util.UUID;

public record NodeView(
        UUID publicId, NodeType nodeType, String name,
        GeometryType geometryType, String geometryData, int sortOrder
) {
    public static NodeView of(RoadmapNode node) {
        return new NodeView(node.getPublicId(), node.getNodeType(), node.getNodeName(),
                node.getGeometryType(), node.getGeometryData(), node.getSortOrder());
    }
}