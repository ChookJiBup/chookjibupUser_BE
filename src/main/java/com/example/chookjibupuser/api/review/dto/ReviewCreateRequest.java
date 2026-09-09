package com.example.chookjibupuser.api.review.dto;

import jakarta.validation.constraints.*;

/**
 * 리뷰 작성 요청. festivalId는 URL 경로의 public_id(QR코드/상세페이지에 담긴 값)로
 * 지정하고, 요청 바디에는 별점/한줄평/현장리뷰 여부를 담는다.
 *
 * <p>{@code onsite}는 프론트가 이 화면에 QR코드로 들어왔는지(축제 현장 방문 인증)를
 * 담아 보낸다 — true인데 로그인이 안 돼있으면 익명 리뷰로 처리되고, false인데
 * 로그인이 안 돼있으면 요청 자체가 거부된다(일반 리뷰는 로그인 필수).</p>
 */
public record ReviewCreateRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank @Size(max = 500) String content,
        @NotNull Boolean onsite
) {
}