package com.example.chookjibupuser.review.dto;

import com.example.chookjibupuser.review.FestivalReview;

import java.time.OffsetDateTime;

/**
 * review 도메인의 순수한 뷰이다. 작성자 닉네임 같은 user 도메인 정보는 담지 않는다
 * (userId만 담아두고, 실제 닉네임 조합은 application 계층이 한다).
 */
public record ReviewView(
        Long reviewId,
        Long userId,
        int rating,
        String content,
        boolean onsite,
        OffsetDateTime createdAt
) {

    public static ReviewView of(FestivalReview review) {
        return new ReviewView(
                review.getReviewId(),
                review.getUserId(),
                review.getRating(),
                review.getContent(),
                review.isOnsite(),
                review.getCreatedAt()
        );
    }
}