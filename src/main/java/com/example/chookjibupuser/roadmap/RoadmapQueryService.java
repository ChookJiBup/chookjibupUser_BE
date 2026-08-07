package com.example.chookjibupuser.roadmap;

import com.example.chookjibupuser.roadmap.dto.RoadmapIconView;
import com.example.chookjibupuser.roadmap.dto.RoadmapView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 축제 로드맵 조회를 처리한다. roadmap 도메인 자신의 저장소만 다룬다
 * (festival_roadmap / roadmap_icon_placement / roadmap_icon_type 세 테이블만) —
 * festival이나 booth 엔티티는 전혀 모르고, related_booth_id도 그냥 숫자로만 들고 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadmapQueryService {

    private final FestivalRoadmapRepository festivalRoadmapRepository;
    private final RoadmapIconPlacementRepository roadmapIconPlacementRepository;
    private final RoadmapIconTypeRepository roadmapIconTypeRepository;

    /**
     * 축제의 로드맵을 조회한다. 관리자/운영자가 아직 로드맵을 만들지 않았으면 empty.
     */
    public Optional<RoadmapView> getRoadmap(Long festivalId) {
        Optional<FestivalRoadmap> roadmap = festivalRoadmapRepository.findById(festivalId);
        if (roadmap.isEmpty()) {
            return Optional.empty();
        }

        String roadmapType = festivalRoadmapRepository.findRoadmapTypeText(festivalId).orElse(null);
        List<RoadmapIconPlacement> placements = roadmapIconPlacementRepository.findByFestivalId(festivalId);
        Map<Long, RoadmapIconType> iconTypeById = loadIconTypes(placements);

        List<RoadmapIconView> icons = placements.stream()
                .map(placement -> toIconView(placement, iconTypeById))
                .toList();

        return Optional.of(new RoadmapView(
                roadmapType,
                roadmap.get().getBaseImageUrl(),
                roadmap.get().getCanvasWidth(),
                roadmap.get().getCanvasHeight(),
                icons
        ));
    }

    private Map<Long, RoadmapIconType> loadIconTypes(List<RoadmapIconPlacement> placements) {
        List<Long> iconTypeIds = placements.stream().map(RoadmapIconPlacement::getIconTypeId).distinct().toList();
        Map<Long, RoadmapIconType> result = new HashMap<>();
        roadmapIconTypeRepository.findAllById(iconTypeIds).forEach(t -> result.put(t.getIconTypeId(), t));
        return result;
    }

    private RoadmapIconView toIconView(RoadmapIconPlacement placement, Map<Long, RoadmapIconType> iconTypeById) {
        RoadmapIconType iconType = iconTypeById.get(placement.getIconTypeId());
        return new RoadmapIconView(
                placement.getPlacementId(),
                iconType == null ? null : iconType.getCode(),
                iconType == null ? null : iconType.getName(),
                iconType == null ? null : iconType.getIconImageUrl(),
                placement.getRelatedBoothId(),
                placement.getPositionX(),
                placement.getPositionY(),
                placement.getRotationDeg(),
                placement.getLabel()
        );
    }
}
