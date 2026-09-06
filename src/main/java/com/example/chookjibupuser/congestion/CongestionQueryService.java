// congestion/CongestionQueryService.java (신규)
package com.example.chookjibupuser.congestion;

import com.example.chookjibupuser.congestion.dto.BoothCongestionView;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 부스별 "지금 혼잡도"(가장 최근 이력 1건)를 조회한다. congestion 도메인 자신의
 * 저장소만 다룬다 — 부스 이름 같은 booth 도메인 정보는 전혀 모른다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CongestionQueryService {

    private final BoothCongestionRepository boothCongestionRepository;

    /** @return boothId -> 그 부스의 최신 혼잡도. 이력이 한 번도 없는 boothId는 결과에서 빠진다. */
    public Map<Long, BoothCongestionView> getLatestByBoothIds(List<Long> boothIds) {
        if (boothIds.isEmpty()) {
            return Map.of();
        }
        return boothCongestionRepository.findLatestByBoothIds(boothIds).stream()
                .map(BoothCongestionView::of)
                .collect(Collectors.toMap(BoothCongestionView::boothId, Function.identity()));
    }
}