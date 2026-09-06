// festival/FestivalQueryService.java (전체)
package com.example.chookjibupuser.festival;

import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalPageView;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    /**
     * @param status ONGOING/UPCOMING/COMPLETED 중 하나(대소문자 무시) 또는 null(전체).
     *               name과 동시에 줄 수 없다.
     * @param sort WISHLIST_COUNT(찜 많은 순)/REVIEW_COUNT(리뷰 많은 순) 또는 null(기본 —
     *             시작일순). name/status와 동시에 줄 수 없다.
     */
    public FestivalPageView searchFestivals(
            String name,
            String status,
            String sort,
            Integer page,
            Integer size
    ) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        LocalDate today = LocalDate.now();

        Page<Festival> result;
        if (name != null && !name.isBlank()) {
            result = festivalRepository.findByFestivalNameContainingIgnoreCaseOrderByStartDateAscFestivalIdAsc(
                    name.trim(), pageable
            );
        } else if (status != null && !status.isBlank()) {
            result = switch (status.trim().toUpperCase()) {
                case "ONGOING" -> festivalRepository
                        .findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAscFestivalIdAsc(
                                today, today, pageable
                        );
                case "UPCOMING" -> festivalRepository
                        .findByStartDateAfterOrderByStartDateAscFestivalIdAsc(today, pageable);
                case "COMPLETED" -> festivalRepository
                        .findByEndDateBeforeOrderByStartDateAscFestivalIdAsc(today, pageable);
                default -> throw new CustomException(ErrorCode.INVALID_REQUEST);
            };
        } else if (sort != null && !sort.isBlank()) {
            result = switch (sort.trim().toUpperCase()) {
                case "WISHLIST_COUNT" -> festivalRepository.findAllOrderByWishlistCountDesc(pageable);
                case "REVIEW_COUNT" -> festivalRepository.findAllOrderByReviewCountDesc(pageable);
                default -> throw new CustomException(ErrorCode.INVALID_REQUEST);
            };
        } else {
            result = festivalRepository.findAllByOrderByStartDateAscFestivalIdAsc(pageable);
        }

        return new FestivalPageView(
                result.getContent().stream().map(FestivalSummaryView::of).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public FestivalDetailView getFestival(Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(ErrorCode.FESTIVAL_NOT_FOUND));
        return FestivalDetailView.of(festival);
    }

    public Map<Long, FestivalSummaryView> getFestivalsByIds(List<Long> festivalIds) {
        Map<Long, FestivalSummaryView> result = new LinkedHashMap<>();
        if (festivalIds.isEmpty()) {
            return result;
        }
        festivalRepository.findByFestivalIdIn(festivalIds)
                .forEach(festival -> result.put(festival.getFestivalId(), FestivalSummaryView.of(festival)));
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