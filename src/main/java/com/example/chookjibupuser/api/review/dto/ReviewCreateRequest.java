package com.example.chookjibupuser.api.review.dto;

import jakarta.validation.constraints.*;

/**
 * 리뷰 작성 요청. festivalId는 URL 경로의 public_id(QR코드에 담긴 값)로 지정하고,
 * 요청 바디에는 별점/한줄평만 담는다.
 */
public record ReviewCreateRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank @Size(max = 500) String content
) {
}
