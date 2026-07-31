package com.example.chookjibupuser.festival;

import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalPageView;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 축제 목록을 로컬 Postgres에서 직접 조회한다(JPA). festival 도메인 자신의
 * 저장소만 다룬다 — 찜(wishlist) 여부 같은 다른 도메인 정보는 전혀 모른다.
 * "찜 여부까지 합쳐서 보여주기"는 api 계층(UserFestivalService)의 책임이다.
 *
 * <p>진행 상태(progress_status)는 더 이상 여기서 날짜로 계산하지 않는다 —
 * 파이썬 파이프라인이 새벽 6시 배치로 미리 계산해둔 컬럼값을 그대로 읽기만 한다.</p>
 *
 * <p>[임시] status/name/region 필터는 일단 뺐다 (비회원 조회 서버 에러 원인 파악 전까지).
 * 페이지네이션만 지원한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalQueryService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final FestivalRepository festivalRepository;

    public FestivalPageView searchFestivals(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));

        Page<FestivalRow> result = festivalRepository.search(pageable);

        return new FestivalPageView(
                result.getContent().stream().map(FestivalSummaryView::of).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * festivalId로 축제 상세를 조회한다. 없으면 FESTIVAL_NOT_FOUND.
     */
    public FestivalDetailView getFestival(Long festivalId) {
        FestivalRow row = festivalRepository.findRowById(festivalId)
                .orElseThrow(() -> new CustomException(ErrorCode.FESTIVAL_NOT_FOUND));
        return FestivalDetailView.of(row);
    }

    /**
     * festivalId 목록으로 축제를 조회한다 (찜 목록 화면에서, 찜한 festivalId들로
     * 상세 정보를 채울 때 다른 도메인의 orchestration 서비스가 이 메서드를 쓴다).
     * 존재하지 않는 id는 결과에서 조용히 빠진다.
     */
    public Map<Long, FestivalSummaryView> getFestivalsByIds(List<Long> festivalIds) {
        Map<Long, FestivalSummaryView> result = new LinkedHashMap<>();
        if (festivalIds.isEmpty()) {
            return result;
        }
        festivalRepository.findRowsByIds(festivalIds)
                .forEach(row -> result.put(row.getFestivalId(), FestivalSummaryView.of(row)));
        return result;
    }

    /**
     * festivalId로 축제가 실제 존재하는지 확인한다 (다른 도메인이 FK 성격의
     * 존재 검증을 해야 할 때 이 메서드를 쓴다 — festival 테이블 구조 자체는
     * 노출하지 않는다).
     */
    public boolean exists(Long festivalId) {
        return festivalRepository.existsById(festivalId);
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return size;
    }
}
