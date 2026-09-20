package com.example.chookjibupuser.review;

import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.review.dto.MyReviewEntryPageView;
import com.example.chookjibupuser.review.dto.MyReviewEntryView;
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
    public ReviewView createReview(Long userId, Long festivalId, int rating, String content, boolean onsite) {
        FestivalReview saved = festivalReviewRepository.save(
                FestivalReview.create(userId, festivalId, rating, content, onsite)
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

    public MyReviewEntryPageView getMyReviews(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<FestivalReview> result = festivalReviewRepository.findByUserIdOrderByReviewIdDesc(
                userId,
                pageable
        );

        return new MyReviewEntryPageView(
                result.getContent().stream()
                        .map(review -> new MyReviewEntryView(
                                review.getReviewId(),
                                review.getFestivalId(),
                                review.getRating(),
                                review.getContent(),
                                review.isOnsite(),
                                review.getCreatedAt()
                        ))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

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

    /**
     * 본인이 작성한 리뷰의 별점/한줄평을 수정한다. 존재하지 않는 reviewId면
     * REVIEW_NOT_FOUND, 존재하지만 본인 리뷰가 아니면 FORBIDDEN을 던진다.
     */
    @Transactional
    public ReviewView updateReview(Long userId, Long reviewId, int rating, String content) {
        FestivalReview review = getOwnedReview(userId, reviewId);
        review.update(rating, content);
        return ReviewView.of(review);
    }

    /**
     * 본인이 작성한 리뷰를 삭제한다. 존재하지 않는 reviewId면 REVIEW_NOT_FOUND,
     * 존재하지만 본인 리뷰가 아니면 FORBIDDEN을 던진다.
     */
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        FestivalReview review = getOwnedReview(userId, reviewId);
        festivalReviewRepository.delete(review);
    }

    /**
     * reviewId로 리뷰를 찾고 작성자(userId)가 맞는지 검증한다. 현장(QR) 익명 리뷰는
     * userId가 null이라 어떤 로그인 사용자와 대조해도 항상 FORBIDDEN이 된다 — 익명
     * 리뷰는 애초에 아무도 수정/삭제할 수 없는 게 의도된 동작이다.
     */
    private FestivalReview getOwnedReview(Long userId, Long reviewId) {
        FestivalReview review = festivalReviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        if (review.getUserId() == null || !review.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return review;
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