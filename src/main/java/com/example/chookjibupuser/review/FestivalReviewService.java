package com.example.chookjibupuser.review;

import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.review.dto.ReviewPageView;
import com.example.chookjibupuser.review.dto.ReviewView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalReviewService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final FestivalReviewRepository festivalReviewRepository;

    @Transactional
    public ReviewView createReview(Long userId, Long festivalId, int rating, String content) {
        FestivalReview saved = festivalReviewRepository.save(
                FestivalReview.create(userId, festivalId, rating, content)
        );
        return ReviewView.of(saved);
    }

    public ReviewPageView getReviews(Long festivalId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<FestivalReview> result = festivalReviewRepository.findByFestivalIdOrderByReviewIdDesc(
                festivalId,
                pageable
        );

        return new ReviewPageView(
                result.getContent().stream().map(ReviewView::of).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * 주어진 festivalId들 각각의 리뷰 개수를 반환한다. 리뷰가 하나도 없는 festivalId는
     * 결과 맵에서 빠진다 — 호출하는 쪽에서 없으면 0으로 취급하면 된다.
     */
    public java.util.Map<Long, Long> getReviewCounts(java.util.List<Long> festivalIds) {
        if (festivalIds.isEmpty()) {
            return java.util.Map.of();
        }
        java.util.Map<Long, Long> result = new java.util.LinkedHashMap<>();
        for (Object[] row : festivalReviewRepository.countByFestivalIdIn(festivalIds)) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
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