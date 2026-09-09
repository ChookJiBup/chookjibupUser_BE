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
import java.util.Set;
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
     * 축제 부스지도. 로드맵 상태가 {@code PUBLISHED}일 때만 내려준다.
     *
     * <p>한동안은 상태를 보지 않고 저장된 지도를 그대로 내려줬다. 관리자 콘솔에 로드맵을
     * {@code PUBLISHED}로 올리는 경로가 아예 없어서, 게이트를 켜 두면 방문객이 어떤 축제의
     * 부스지도도 볼 수 없었기 때문이다. 지금은 관리자 콘솔에 「방문객에게 공개」/「공개 해제」
     * 토글이 생겼고 저장해도 공개 상태가 유지되므로, 다시 상태를 보고 내려준다. 관리자가
     * 아직 검토 중인 배치가 방문객 화면에 먼저 나가지 않게 하려는 것이다.</p>
     *
     * <p>AI가 찾아만 둔 초안 노드는 아래 {@code isConfirmed} 필터가 걸러내므로, 공개된
     * 로드맵에서도 나가는 것은 관리자가 확인한 노드뿐이다.</p>
     *
     * @return 로드맵이 없거나 아직 공개되지 않았으면 null. 축제 상세 응답의 roadmap이 null이 되고,
     *         방문객 화면은 「아직 배치도가 공개되지 않았어요」를 보여 준다.
     */
    public RoadmapView getRoadmap(Long festivalId) {
        FestivalRoadmap roadmap = festivalRoadmapRepository.findByFestivalId(festivalId).orElse(null);
        if (roadmap == null || roadmap.getStatus() != RoadmapStatus.PUBLISHED) {
            return null;
        }

        List<RoadmapNode> confirmedNodes = roadmapNodeRepository
                .findByRoadmapIdOrderBySortOrderAsc(roadmap.getId())
                .stream().filter(RoadmapNode::isConfirmed).toList();

        Map<UUID, NodeView> boothsByPublicId = confirmedNodes.stream()
                .filter(RoadmapNode::isBooth)
                .collect(java.util.stream.Collectors.toMap(RoadmapNode::getPublicId, NodeView::of));

        List<ZoneView> zones = new ArrayList<>(roadmap.getZones().stream()
                .map(zone -> toZoneView(zone, boothsByPublicId)).toList());
        /*
          구역에 묶이지 않은 부스는 어디에도 담기지 못해 방문객 화면에서 통째로 사라졌다.
          관리자가 지도에 찍기만 하고 구역으로 묶지 않은 부스가 그렇다. 남는 부스는
          마지막에 «구역 미지정»으로 모아 보여 준다(관리자 대시보드와 같은 방식).
        */
        Set<UUID> zonedBoothIds = roadmap.getZones().stream()
                .flatMap(zone -> zone.boothNodeIds().stream())
                .collect(java.util.stream.Collectors.toSet());
        List<NodeView> unzonedBooths = confirmedNodes.stream()
                .filter(RoadmapNode::isBooth)
                .filter(node -> !zonedBoothIds.contains(node.getPublicId()))
                .map(NodeView::of)
                .toList();
        if (!unzonedBooths.isEmpty()) {
            zones.add(new ZoneView(null, "구역 미지정", zones.size(), unzonedBooths));
        }

        List<NodeView> otherNodes = confirmedNodes.stream()
                .filter(node -> !node.isBooth()).map(NodeView::of).toList();

        FestivalMap currentMap = findCurrentMap(roadmap);

        return new RoadmapView(
                roadmap.getPublicId(),
                resolveMapImageUrl(currentMap),
                zones,
                otherNodes,
                resolvePresentation(currentMap)
        );
    }

    /**
     * 로드맵이 지금 쓰고 있는 지도 한 장.
     *
     * <p>축제로 지도를 찾으면 안 된다. {@code festival_maps}는 지도를 교체해도 예전 판을
     * {@code REPLACED}로 남겨 두는 이력 테이블이라, 한 번이라도 배치도를 갈아끼운 축제는
     * 축제당 행이 여러 개다. 그런 축제에서 축제 단위 단건 조회가 결과 개수 예외로 터지면서
     * 축제 상세 API가 통째로 500이 됐다. 로드맵이 가리키는 {@code currentMapId}로 집으면
     * 이력이 몇 장이든 지금 판 하나만 나온다.</p>
     */
    private FestivalMap findCurrentMap(FestivalRoadmap roadmap) {
        if (roadmap.getCurrentMapId() == null) {
            return null;
        }
        return festivalMapRepository.findById(roadmap.getCurrentMapId()).orElse(null);
    }

    private ZoneView toZoneView(RoadmapZone zone, Map<UUID, NodeView> boothsByPublicId) {
        List<NodeView> booths = zone.boothNodeIds().stream()
                .map(boothsByPublicId::get)
                .filter(java.util.Objects::nonNull)
                .toList();
        return new ZoneView(zone.zoneId(), zone.name(), zone.sortOrder(), booths);
    }

    private String resolveMapImageUrl(FestivalMap currentMap) {
        if (currentMap == null) {
            return null;
        }
        return resolveImageUrl(currentMap.getDisplayImageKey());
    }

    /**
     * 관리자가 맞춰 둔 부지 경계와 팜플렛을 그릴 수 있는 형태로 옮긴다.
     *
     * <p>표시 설정이 없거나 팜플렛이 꺼져 있으면 그 부분만 빠진다. 지도 자체는 부스만으로도
     * 그릴 수 있으므로, 여기서 문제가 생겨도 로드맵 전체를 막지 않는다.</p>
     */
    private PresentationView resolvePresentation(FestivalMap currentMap) {
        if (currentMap == null) {
            return null;
        }
        FestivalMapPresentation presentation = presentationRepository.findByMapId(currentMap.getId())
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
