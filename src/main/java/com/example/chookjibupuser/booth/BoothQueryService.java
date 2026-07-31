package com.example.chookjibupuser.booth;

import com.example.chookjibupuser.booth.dto.BoothView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 부스 조회를 처리한다. booth 도메인 자신의 저장소만 다룬다 — 혼잡도(congestion)
 * 정보는 절대 여기서 채우지 않는다. 혼잡도와 합치는 건 api/application 계층의 책임이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoothQueryService {

    private final BoothInfoRepository boothInfoRepository;

    /**
     * 축제의 부스 목록을 조회한다. 등록된 부스가 없으면 빈 목록.
     */
    public List<BoothView> getBooths(Long festivalId) {
        return boothInfoRepository.findByFestivalId(festivalId).stream()
                .map(BoothView::of)
                .toList();
    }
}
