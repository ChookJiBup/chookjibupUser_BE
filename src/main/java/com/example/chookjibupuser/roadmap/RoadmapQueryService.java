// roadmap/RoadmapQueryService.java
package com.example.chookjibupuser.roadmap;

import com.example.chookjibupuser.roadmap.dto.NodeView;
import com.example.chookjibupuser.roadmap.dto.RoadmapView;
import com.example.chookjibupuser.roadmap.dto.ZoneView;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * [중요] 실시간 대기시간/혼잡도는 관리자 백엔드에 그걸 갱신하는 기능 자체가 없어서
 * 포함하지 않는다. 지금은 "부스가 어디에 있는지"(로드맵 위치 정보)까지만 제공한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadmapQueryService {

    private final FestivalRoadmapRepository festivalRoadmapRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final FestivalMapRepository festivalMapRepository;
    private final MapImageProperties mapImageProperties;

    /** @return 로드맵이 없거나 아직 PUBLISHED 안 됐으면 null. */
    public RoadmapView getRoadmap(Long festivalId) {
        FestivalRoadmap roadmap = festivalRoadmapRepository.findByFestivalId(festivalId).orElse(null);
        if (roadmap == null || !roadmap.isPublished()) {
            return null;
        }

        List<RoadmapNode> confirmedNodes = roadmapNodeRepository
                .findByRoadmapIdOrderBySortOrderAsc(roadmap.getId())
                .stream().filter(RoadmapNode::isConfirmed).toList();

        Map<UUID, NodeView> boothsByPublicId = confirmedNodes.stream()
                .filter(RoadmapNode::isBooth)
                .collect(java.util.stream.Collectors.toMap(RoadmapNode::getPublicId, NodeView::of));

        List<ZoneView> zones = roadmap.getZones().stream()
                .map(zone -> toZoneView(zone, boothsByPublicId)).toList();

        List<NodeView> otherNodes = confirmedNodes.stream()
                .filter(node -> !node.isBooth()).map(NodeView::of).toList();

        return new RoadmapView(roadmap.getPublicId(), resolveMapImageUrl(festivalId), zones, otherNodes);
    }

    private ZoneView toZoneView(RoadmapZone zone, Map<UUID, NodeView> boothsByPublicId) {
        List<NodeView> booths = zone.boothNodeIds().stream()
                .map(boothsByPublicId::get)
                .filter(java.util.Objects::nonNull)
                .toList();
        return new ZoneView(zone.zoneId(), zone.name(), zone.sortOrder(), booths);
    }

    private String resolveMapImageUrl(Long festivalId) {
        if (!StringUtils.hasText(mapImageProperties.imageBaseUrl())) {
            return null;
        }
        return festivalMapRepository.findByFestivalId(festivalId)
                .map(FestivalMap::getDisplayImageKey)
                .filter(StringUtils::hasText)
                .map(key -> mapImageProperties.imageBaseUrl().replaceAll("/+$", "") + "/" + key)
                .orElse(null);
    }
}