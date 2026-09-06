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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * "이 축제 지금 얼마나 혼잡한지"를 조회하는 유스케이스를 처리한다. booth 도메인(부스 이름)과
 * congestion 도메인(최신 혼잡도 이력)은 서로의 존재를 모른다 — 여기서 처음 합친다.
 * 관리자 백엔드의 BoothCongestionQueryApplicationService와 계산 로직을 동일하게 맞췄다.
 */
@Service
@RequiredArgsConstructor
public class UserCongestionService {

    private final FestivalQueryService festivalQueryService;
    private final BoothQueryService boothQueryService;
    private final CongestionQueryService congestionQueryService;

    public FestivalCongestionResponse getCongestion(UUID festivalPublicId) {
        Long festivalId = festivalQueryService.getFestivalIdByPublicId(festivalPublicId);

        List<BoothView> booths = boothQueryService.getBoothsByFestivalId(festivalId);
        Map<Long, BoothCongestionView> latestByBooth = congestionQueryService.getLatestByBoothIds(
                booths.stream().map(BoothView::boothId).toList()
        );

        int activeQueueCount = 0;
        int waitSum = 0;
        int waitCount = 0;
        LocalDateTime newest = null;

        List<BoothCongestionResponse> items = booths.stream().map(booth -> {
            BoothCongestionView congestion = latestByBooth.get(booth.boothId());
            return new BoothCongestionResponse(
                    booth.boothId(),
                    booth.name(),
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

        return new FestivalCongestionResponse(
                newest,
                booths.isEmpty() ? null : activeQueueCount,
                waitCount == 0 ? null : waitSum / waitCount,
                ranking,
                items
        );
    }
}