// api/festival/dto/RoadmapZoneResponse.java (신규)
package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.roadmap.dto.ZoneView;
import java.util.List;
import java.util.UUID;

public record RoadmapZoneResponse(UUID zoneId, String name, int sortOrder, List<RoadmapNodeResponse> booths) {
    public static RoadmapZoneResponse from(ZoneView view) {
        return new RoadmapZoneResponse(view.zoneId(), view.name(), view.sortOrder(),
                view.booths().stream().map(RoadmapNodeResponse::from).toList());
    }
}