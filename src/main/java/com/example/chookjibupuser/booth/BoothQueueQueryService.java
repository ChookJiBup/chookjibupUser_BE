package com.example.chookjibupuser.booth;

import com.example.chookjibupuser.booth.dto.BoothQueueView;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 부스별 대기열(줄끝) 현재 상태를 조회한다. booth 도메인 자신의 저장소만 다룬다 —
 * 혼잡도(congestion)는 전혀 모른다. "혼잡도 + 줄 길이"를 합치는 건 application
 * 계층(UserCongestionService)의 책임이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoothQueueQueryService {

    private final BoothQueueRepository boothQueueRepository;

    /** @return boothId -> 그 부스의 대기열 상태. 대기열이 아직 안 그려진 boothId는 결과에서 빠진다. */
    public Map<Long, BoothQueueView> getQueuesByBoothIds(List<Long> boothIds) {
        if (boothIds.isEmpty()) {
            return Map.of();
        }
        return boothQueueRepository.findByBoothIdIn(boothIds).stream()
                .map(BoothQueueView::of)
                .collect(Collectors.toMap(BoothQueueView::boothId, Function.identity()));
    }
}