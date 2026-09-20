package com.example.chookjibupuser.api.review.dto;

import jakarta.validation.constraints.*;

/**
 * 리뷰 수정 요청. 별점/한줄평만 바꿀 수 있다 — 어떤 축제의 리뷰인지(festivalId),
 * 현장(QR) 리뷰였는지(onsite)는 작성 시점에 고정되고 수정 대상이 아니다.
 */
public record ReviewUpdateRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank @Size(max = 500) String content
) {
}