package com.example.chookjibupuser.booth;

import com.example.chookjibupuser.booth.dto.BoothView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 승인된 부스 마스터를 조회한다. booth 도메인 자신의 저장소만 다룬다 — 혼잡도(congestion)는
 * 전혀 모른다. "부스 정보 + 혼잡도"를 합치는 건 application 계층의 책임이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoothQueryService {

    private final BoothInfoRepository boothInfoRepository;

    public List<BoothView> getBoothsByFestivalId(Long festivalId) {
        return boothInfoRepository.findByFestivalIdOrderByIdAsc(festivalId).stream()
                .map(BoothView::of)
                .toList();
    }
}