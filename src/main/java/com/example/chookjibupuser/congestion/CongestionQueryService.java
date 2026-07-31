package com.example.chookjibupuser.congestion;

import com.example.chookjibupuser.congestion.dto.BoothCongestionView;
import com.example.chookjibupuser.congestion.dto.FestivalCongestionView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 축제/부스 혼잡도 조회를 처리한다. congestion 도메인 자신의 저장소만 다룬다 —
 * booth_id/festival_id는 그냥 숫자로만 다루고 booth나 festival 엔티티는 전혀 모른다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CongestionQueryService {

    private final BoothCongestionRepository boothCongestionRepository;
    private final FestivalCongestionRepository festivalCongestionRepository;

    /**
     * 주어진 부스들의 최신 혼잡도를 boothId 기준으로 묶어서 반환한다.
     * 혼잡도 기록이 없는 부스는 결과 Map에 아예 없다(호출한 쪽에서 get()이 null이면
     * "혼잡도 정보 없음"으로 처리하면 된다).
     */
    public Map<Long, BoothCongestionView> getLatestBoothCongestion(List<Long> boothIds) {
        if (boothIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, BoothCongestionView> result = new HashMap<>();
        boothCongestionRepository.findLatestByBoothIds(boothIds).forEach(row ->
                result.put(row.getBoothId(), new BoothCongestionView(
                        row.getCongestionLevel(),
                        row.getWaitMinutes(),
                        row.getUpdatedAt()
                ))
        );
        return result;
    }

    /**
     * 축제의 최신 혼잡도를 조회한다. 기록이 없으면 empty.
     */
    public Optional<FestivalCongestionView> getLatestFestivalCongestion(Long festivalId) {
        return festivalCongestionRepository.findLatestByFestivalId(festivalId)
                .map(row -> new FestivalCongestionView(row.getCongestionLevel(), row.getUpdatedAt()));
    }
}
