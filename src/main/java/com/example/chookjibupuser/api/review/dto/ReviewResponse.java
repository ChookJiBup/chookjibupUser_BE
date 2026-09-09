package com.example.chookjibupuser.api.review.dto;

import com.example.chookjibupuser.review.dto.ReviewView;

import java.time.OffsetDateTime;

/**
 * {@code reviewerName}은 로그인 리뷰면 작성자 닉네임, 익명(현장) 리뷰면 "현장 방문자"로
 * 고정된 표시명이다 — 실제 계정을 알 수 없는 익명 작성자를 화면에 자연스럽게 보여주기 위함.
 * {@code onsite}가 true인 리뷰는 상세페이지에서 인증 배지로 구분해서 보여준다.
 */
public record ReviewResponse(
        Long reviewId,
        String reviewerName,
        int rating,
        String content,
        boolean onsite,
        OffsetDateTime createdAt
) {

    private static final String ANONYMOUS_REVIEWER_NAME = "현장 방문자";

    public static ReviewResponse from(ReviewView view, String reviewerNickname) {
        String reviewerName = view.userId() == null
                ? ANONYMOUS_REVIEWER_NAME
                : (reviewerNickname != null ? reviewerNickname : "탈퇴한 사용자");
        return new ReviewResponse(
                view.reviewId(),
                reviewerName,
                view.rating(),
                view.content(),
                view.onsite(),
                view.createdAt()
        );
    }
}