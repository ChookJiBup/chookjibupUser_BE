// roadmap/RoadmapZone.java
package com.example.chookjibupuser.roadmap;

import java.util.List;
import java.util.UUID;

/** 관리자 백엔드의 RoadmapZone record와 필드가 정확히 같아야 역직렬화된다. */
public record RoadmapZone(
        UUID zoneId,
        String name,
        int sortOrder,
        List<UUID> boothNodeIds
) {
    public RoadmapZone {
        boothNodeIds = boothNodeIds == null ? List.of() : List.copyOf(boothNodeIds);
    }
}