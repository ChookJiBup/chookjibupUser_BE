package com.example.chookjibupuser.api.review.dto;

import com.example.chookjibupuser.review.dto.ReviewView;

import java.time.OffsetDateTime;

public record ReviewResponse(
        Long reviewId,
        int rating,
        String content,
        OffsetDateTime createdAt
) {

    public static ReviewResponse from(ReviewView view) {
        return new ReviewResponse(view.reviewId(), view.rating(), view.content(), view.createdAt());
    }
}
