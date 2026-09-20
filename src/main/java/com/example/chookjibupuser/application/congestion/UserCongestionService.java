// application/congestion/UserCongestionService.java (신규)
package com.example.chookjibupuser.application.congestion;

import com.example.chookjibupuser.api.congestion.dto.BoothCongestionResponse;
import com.example.chookjibupuser.api.congestion.dto.FestivalCongestionResponse;
import com.example.chookjibupuser.booth.BoothQueryService;
import com.example.chookjibupuser.booth.dto.BoothView;
import com.example.chookjibupuser.congestion.BoothCongestionLevel;
import com.example.chookjibupuser.congestion.CongestionQueryService;
import com.example.chookjibupuser.congestion.dto.BoothCongestionView;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.roadmap.RoadmapQueryService;
import com.example.chookjibupuser.roadmap.dto.BoothZoneView;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * "이 축제 지금 얼마나 혼잡한지"를 조회하는 유스케이스를 처리한다. booth 도메인(부스 이름),
 * congestion 도메인(최신 혼잡도 이력), roadmap 도메인(부스가 속한 구역)은 서로의 존재를
 * 모른다 — 여기서 처음 합친다.
 * 관리자 백엔드의 BoothCongestionQueryApplicationService와 계산 로직을 동일하게 맞췄다.
 */
@Service
@RequiredArgsConstructor
public class UserCongestionService {

    private final FestivalQueryService festivalQueryService;
    private final BoothQueryService boothQueryService;
    private final CongestionQueryService congestionQueryService;
    private final RoadmapQueryService roadmapQueryService;

    public FestivalCongestionResponse getCongestion(UUID festivalPublicId) {
        Long festivalId = festivalQueryService.getFestivalIdByPublicId(festivalPublicId);

        List<BoothView> booths = boothQueryService.getBoothsByFestivalId(festivalId);
        Map<Long, BoothCongestionView> latestByBooth = congestionQueryService.getLatestByBoothIds(
                booths.stream().map(BoothView::boothId).toList()
        );
        /*
          구역은 부스 이름이 아니라 지도 노드 id로 잇는다. 이름으로 맞춰 붙이던 시절에는
          이름이 조금만 달라도 구역이 안 붙었고, 같은 이름의 부스 둘은 구분이 안 됐다.
        */
        Map<Long, BoothZoneView> zonesByNodeId = roadmapQueryService.getBoothZones(
                festivalId,
                booths.stream().map(BoothView::roadmapNodeId).toList()
        );

        int activeQueueCount = 0;
        int waitSum = 0;
        int waitCount = 0;
        LocalDateTime newest = null;

        List<BoothCongestionResponse> items = booths.stream().map(booth -> {
            BoothCongestionView congestion = latestByBooth.get(booth.boothId());
            BoothZoneView zone = booth.roadmapNodeId() == null
                    ? null
                    : zonesByNodeId.get(booth.roadmapNodeId());
            return new BoothCongestionResponse(
                    booth.boothId(),
                    booth.name(),
                    zone == null ? null : zone.nodePublicId(),
                    zone == null ? null : zone.zoneId(),
                    zone == null ? null : zone.zoneName(),
                    congestion == null ? null : congestion.level(),
                    congestion == null ? null : congestion.waitMinutes(),
                    congestion == null ? null : congestion.updatedAt()
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

        /*
          축제 전체 혼잡도. ranking이 아니라 booths 전체를 본다 — 대기시간이 아직 없는 부스도
          등급은 매겨져 있을 수 있는데, ranking만 보면 그런 부스가 통째로 빠져 「정보 없음」이
          된다.
        */
        BoothCongestionLevel overallLevel = BoothCongestionLevel.highest(
                items.stream().map(BoothCongestionResponse::congestionLevel).toList()
        );

        return new FestivalCongestionResponse(
                newest,
                overallLevel,
                booths.isEmpty() ? null : activeQueueCount,
                waitCount == 0 ? null : waitSum / waitCount,
                ranking,
                items
        );
    }
}
