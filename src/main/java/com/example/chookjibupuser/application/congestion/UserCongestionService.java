package com.example.chookjibupuser.application.congestion;

import com.example.chookjibupuser.api.congestion.dto.BoothCongestionResponse;
import com.example.chookjibupuser.api.congestion.dto.FestivalCongestionResponse;
import com.example.chookjibupuser.booth.BoothQueryService;
import com.example.chookjibupuser.booth.BoothQueueQueryService;
import com.example.chookjibupuser.booth.dto.BoothQueueView;
import com.example.chookjibupuser.booth.dto.BoothView;
import com.example.chookjibupuser.congestion.BoothCongestionLevel;
import com.example.chookjibupuser.congestion.CongestionQueryService;
import com.example.chookjibupuser.congestion.dto.BoothCongestionView;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.roadmap.RoadmapQueryService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * "이 축제 지금 얼마나 혼잡한지"를 조회하는 유스케이스를 처리한다. booth 도메인(부스 이름),
 * congestion 도메인(최신 혼잡도 이력), booth 도메인의 대기열(줄끝/줄 길이), 그리고 roadmap
 * 도메인(배치도 노드의 공개 UUID)은 서로의 존재를 모른다 — 여기서 처음 합친다.
 */
@Service
@RequiredArgsConstructor
public class UserCongestionService {

    private final FestivalQueryService festivalQueryService;
    private final BoothQueryService boothQueryService;
    private final CongestionQueryService congestionQueryService;
    private final BoothQueueQueryService boothQueueQueryService;
    private final RoadmapQueryService roadmapQueryService;

    public FestivalCongestionResponse getCongestion(UUID festivalPublicId) {
        Long festivalId = festivalQueryService.getFestivalIdByPublicId(festivalPublicId);

        List<BoothView> booths = boothQueryService.getBoothsByFestivalId(festivalId);
        List<Long> boothIds = booths.stream().map(BoothView::boothId).toList();
        Map<Long, BoothCongestionView> latestByBooth = congestionQueryService.getLatestByBoothIds(boothIds);
        Map<Long, BoothQueueView> queueByBooth = boothQueueQueryService.getQueuesByBoothIds(boothIds);

        // 부스가 찍힌 배치도 노드의 내부 PK(roadmapNodeId) -> 공개 UUID. 프론트는 배치도
        // 응답의 노드 publicId와 이 값으로 잇는다(부스 이름은 겹칠 수 있어 안 쓴다).
        List<Long> roadmapNodeIds = booths.stream()
                .map(BoothView::roadmapNodeId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, UUID> nodePublicIdsByNodeId = roadmapQueryService.getPublicIdsByNodeIds(roadmapNodeIds);

        int activeQueueCount = 0;
        int waitSum = 0;
        int waitCount = 0;
        LocalDateTime newest = null;

        List<BoothCongestionResponse> items = booths.stream().map(booth -> {
            BoothCongestionView congestion = latestByBooth.get(booth.boothId());
            BoothQueueView queue = queueByBooth.get(booth.boothId());
            UUID roadmapNodePublicId = booth.roadmapNodeId() == null
                    ? null
                    : nodePublicIdsByNodeId.get(booth.roadmapNodeId());
            return new BoothCongestionResponse(
                    booth.boothId(),
                    booth.name(),
                    roadmapNodePublicId,
                    congestion == null ? null : congestion.level(),
                    congestion == null ? null : congestion.waitMinutes(),
                    congestion == null ? null : congestion.updatedAt(),
                    queue == null ? null : queue.tailLatitude(),
                    queue == null ? null : queue.tailLongitude(),
                    queue == null ? null : queue.queueTailMeters(),
                    queue == null ? null : queue.pathGeometry(),
                    queue == null ? null : queue.updatedAt()
            );
        }).toList();

        for (BoothCongestionResponse item : items) {
            if (item.congestionLevel() != null && item.congestionLevel() != BoothCongestionLevel.LOW
                    && item.waitMinutes() != null && item.waitMinutes() > 0) {
                activeQueueCount++;
            }
            if (item.waitMinutes() != null) {
                waitSum += item.waitMinutes();
                waitCount++;
            }
            if (item.updatedAt() != null && (newest == null || item.updatedAt().isAfter(newest))) {
                newest = item.updatedAt();
            }
        }

        // 부스 예상 대기시간 랭킹(대기시간 긴 순). 대기시간 정보가 없는 부스는 제외한다.
        List<BoothCongestionResponse> ranking = items.stream()
                .filter(item -> item.waitMinutes() != null)
                .sorted(Comparator.comparingInt(BoothCongestionResponse::waitMinutes).reversed())
                .toList();

        return new FestivalCongestionResponse(
                newest,
                booths.isEmpty() ? null : activeQueueCount,
                waitCount == 0 ? null : waitSum / waitCount,
                ranking,
                items
        );
    }
}