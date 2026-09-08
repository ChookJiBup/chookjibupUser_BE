// roadmap/RoadmapQueryService.java
package com.example.chookjibupuser.roadmap;

import com.example.chookjibupuser.roadmap.dto.NodeView;
import com.example.chookjibupuser.roadmap.dto.PresentationView;
import com.example.chookjibupuser.roadmap.dto.RoadmapView;
import com.example.chookjibupuser.roadmap.dto.ZoneView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final FestivalMapPresentationRepository presentationRepository;
    private final MapImageProperties mapImageProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 축제 부스지도.
     *
     * <p>예전에는 로드맵이 {@code PUBLISHED}일 때만 내려줬는데, 관리자 쪽에 그 상태로 올리는
     * 경로가 아예 없어 방문객은 어떤 축제의 부스지도도 볼 수 없었다. 관리자가 저장한 지도를
     * 그대로 보여 준다. AI가 찾아만 둔 초안 노드는 아래 {@code isConfirmed} 필터가 계속
     * 걸러내므로, 나가는 것은 관리자가 확인한 노드뿐이다.</p>
     *
     * @return 로드맵이 없으면 null.
     */
    public RoadmapView getRoadmap(Long festivalId) {
        FestivalRoadmap roadmap = festivalRoadmapRepository.findByFestivalId(festivalId).orElse(null);
        if (roadmap == null) {
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

        return new RoadmapView(
                roadmap.getPublicId(),
                resolveMapImageUrl(festivalId),
                zones,
                otherNodes,
                resolvePresentation(festivalId)
        );
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

    /**
     * 관리자가 맞춰 둔 부지 경계와 팜플렛을 그릴 수 있는 형태로 옮긴다.
     *
     * <p>표시 설정이 없거나 팜플렛이 꺼져 있으면 그 부분만 빠진다. 지도 자체는 부스만으로도
     * 그릴 수 있으므로, 여기서 문제가 생겨도 로드맵 전체를 막지 않는다.</p>
     */
    private PresentationView resolvePresentation(Long festivalId) {
        FestivalMapPresentation presentation = festivalMapRepository.findByFestivalId(festivalId)
                .flatMap(map -> presentationRepository.findByMapId(map.getId()))
                .orElse(null);
        if (presentation == null) {
            return null;
        }
        List<PresentationView.LatLngView> boundary = readBoundary(presentation);
        PresentationView.OverlayView overlay = readOverlay(presentation);
        if (boundary == null && overlay == null) {
            return null;
        }
        return new PresentationView(boundary, overlay);
    }

    private List<PresentationView.LatLngView> readBoundary(FestivalMapPresentation presentation) {
        if (!StringUtils.hasText(presentation.getBoundaryGeometry())) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(presentation.getBoundaryGeometry());
            JsonNode points = root.get("points");
            if (points == null || !points.isArray() || points.size() < 3) {
                return null;
            }
            List<PresentationView.LatLngView> parsed = new ArrayList<>();
            for (JsonNode point : points) {
                JsonNode lat = point.get("lat");
                JsonNode lng = point.get("lng");
                if (lat == null || lng == null || !lat.isNumber() || !lng.isNumber()) {
                    return null;
                }
                parsed.add(new PresentationView.LatLngView(
                        lat.decimalValue(),
                        lng.decimalValue()
                ));
            }
            return parsed;
        } catch (Exception exception) {
            // 경계 JSON이 깨졌다고 지도 전체를 막지는 않는다.
            return null;
        }
    }

    private PresentationView.OverlayView readOverlay(FestivalMapPresentation presentation) {
        MapOverlayProjection.Corners corners = MapOverlayProjection.corners(presentation);
        if (corners == null) {
            return null;
        }
        String imageUrl = resolveImageUrl(presentation.getOverlayImageKey());
        if (imageUrl == null) {
            return null;
        }
        BigDecimal opacity = presentation.getOverlayOpacity() == null
                ? BigDecimal.ONE
                : presentation.getOverlayOpacity();
        return new PresentationView.OverlayView(
                imageUrl,
                presentation.getOverlayImageWidth(),
                presentation.getOverlayImageHeight(),
                corners.topLeft(),
                corners.topRight(),
                corners.bottomRight(),
                corners.bottomLeft(),
                opacity,
                presentation.isClipToBoundary()
        );
    }

    private String resolveImageUrl(String objectKey) {
        if (!StringUtils.hasText(mapImageProperties.imageBaseUrl())
                || !StringUtils.hasText(objectKey)) {
            return null;
        }
        return mapImageProperties.imageBaseUrl().replaceAll("/+$", "") + "/" + objectKey;
    }
}
