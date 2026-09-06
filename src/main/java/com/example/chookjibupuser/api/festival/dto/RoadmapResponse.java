// api/festival/dto/RoadmapResponse.java (수정)
package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.roadmap.dto.RoadmapView;
import java.util.List;
import java.util.UUID;

/** 실시간 대기시간/혼잡도는 포함하지 않는다 — 관리자 백엔드에 그 기능이 없음. */
public record RoadmapResponse(
        UUID roadmapPublicId, String mapImageUrl,
        List<RoadmapZoneResponse> zones, List<RoadmapNodeResponse> otherNodes
) {
    public static RoadmapResponse from(RoadmapView view) {
        return new RoadmapResponse(view.roadmapPublicId(), view.mapImageUrl(),
                view.zones().stream().map(RoadmapZoneResponse::from).toList(),
                view.otherNodes().stream().map(RoadmapNodeResponse::from).toList());
    }
}