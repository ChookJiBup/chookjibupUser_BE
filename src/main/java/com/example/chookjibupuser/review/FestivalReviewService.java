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

/**
 * 축제 리뷰(별점+한줄평) 작성/조회를 처리한다. review 도메인 자신의 저장소만 다룬다 —
 * festivalId가 실제 존재하는 축제인지, 리뷰를 쓰려는 userId가 실제 로그인한 사용자인지는
 * 이 서비스가 아니라 application 계층(UserReviewService)이 검증한다.
 */
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
