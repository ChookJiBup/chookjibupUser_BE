package com.example.chookjibupuser.review.dto;

import com.example.chookjibupuser.review.FestivalReview;

import java.time.OffsetDateTime;

/**
 * review 도메인의 순수한 뷰이다. 작성자 닉네임 같은 user 도메인 정보는 담지 않는다
 * (필요해지면 application 계층에서 조합한다 — 지금은 review_id 기준으로만 보여준다).
 */
public record ReviewView(
        Long reviewId,
        Long userId,
        int rating,
        String content,
        OffsetDateTime createdAt
) {

    public static ReviewView of(FestivalReview review) {
        return new ReviewView(
                review.getReviewId(),
                review.getUserId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
