package com.example.chookjibupuser.festival;

import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalPageView;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalQueryService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final FestivalRepository festivalRepository;
    /** 서비스 기준 시간대(Asia/Seoul) 시계. 진행 상태 계산의 "오늘"을 여기서만 얻는다. */
    private final Clock clock;

    /**
     * @param status ONGOING/UPCOMING/COMPLETED 중 하나(대소문자 무시) 또는 null(전체).
     *               region/sort와 자유롭게 조합 가능하다(예: "경기도의 진행중인 축제를
     *               찜 많은 순으로"). name과는 동시에 줄 수 없다 — 검색은 별도 화면이다.
     * @param region 시/도 이름(예: "서울", "경기", "강원" 등, 정식 명칭 일부만 와도 됨) 또는
     *               null(전국). status/sort와 자유롭게 조합 가능하다. name과는 동시에 줄 수 없다.
     * @param sort WISHLIST_COUNT(찜 많은 순)/REVIEW_COUNT(리뷰 많은 순) 또는 null(기본 —
     *             시작일순). status/region과 자유롭게 조합 가능하다. name과는 동시에 줄 수 없다.
     */
    public FestivalPageView searchFestivals(
            String name,
            String status,
            String region,
            String sort,
            Integer page,
            Integer size
    ) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        LocalDate today = today();

        Page<Festival> result;
        if (name != null && !name.isBlank()) {
            result = festivalRepository.findByFestivalNameContainingIgnoreCaseOrderByStartDateAscFestivalIdAsc(
                    name.trim(), pageable
            );
        } else {
            result = festivalRepository.findByRegionAndStatusAndSort(
                    region != null && !region.isBlank() ? region.trim() : null,
                    normalizeStatus(status),
                    normalizeSort(sort),
                    today,
                    pageable
            );
        }

        return new FestivalPageView(
                result.getContent().stream().map(festival -> FestivalSummaryView.of(festival, today)).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return null;
        String upper = status.trim().toUpperCase();
        if (!upper.equals("ONGOING") && !upper.equals("UPCOMING") && !upper.equals("COMPLETED")) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return upper;
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) return null;
        String upper = sort.trim().toUpperCase();
        if (!upper.equals("WISHLIST_COUNT") && !upper.equals("REVIEW_COUNT")) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return upper;
    }

    public FestivalDetailView getFestival(Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(ErrorCode.FESTIVAL_NOT_FOUND));
        return FestivalDetailView.of(festival, today());
    }

    public Map<Long, FestivalSummaryView> getFestivalsByIds(List<Long> festivalIds) {
        Map<Long, FestivalSummaryView> result = new LinkedHashMap<>();
        if (festivalIds.isEmpty()) {
            return result;
        }
        LocalDate today = today();
        festivalRepository.findByFestivalIdIn(festivalIds)
                .forEach(festival -> result.put(festival.getFestivalId(), FestivalSummaryView.of(festival, today)));
        return result;
    }

    public boolean exists(Long festivalId) {
        return festivalRepository.existsById(festivalId);
    }

    public Long getFestivalIdByPublicId(UUID publicId) {
        return festivalRepository.findByPublicId(publicId)
                .map(Festival::getFestivalId)
                .orElseThrow(() -> new CustomException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

    /**
     * 진행 상태의 기준이 되는 "오늘". 축제 일정이 한국 날짜로 등록되므로 서버 기본
     * 시간대(운영 EC2는 UTC)가 아니라 Asia/Seoul 기준으로 읽어야 한다.
     */
    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private int normalizePage(Integer page) {
        if (page == null) return DEFAULT_PAGE;
        if (page < 0) throw new CustomException(ErrorCode.INVALID_REQUEST);
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) return DEFAULT_SIZE;
        if (size < 1 || size > MAX_SIZE) throw new CustomException(ErrorCode.INVALID_REQUEST);
        return size;
    }
}