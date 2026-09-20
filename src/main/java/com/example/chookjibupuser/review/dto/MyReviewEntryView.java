package com.example.chookjibupuser.review.dto;

import java.time.OffsetDateTime;

/**
 * "내가 쓴 리뷰" 목록에서 쓰는, review 도메인 내부용 순수 뷰이다. WishlistEntryView와
 * 같은 패턴 — festivalId만 갖고 있고 축제 상세 정보는 모른다. 상세 정보 병합은
 * application 계층(UserReviewService)의 책임이다.
 */
public record MyReviewEntryView(
        Long reviewId,
        Long festivalId,
        int rating,
        String content,
        boolean onsite,
        OffsetDateTime createdAt
) {
}