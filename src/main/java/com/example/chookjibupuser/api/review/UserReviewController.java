package com.example.chookjibupuser.api.review;

import com.example.chookjibupuser.api.review.dto.ReviewCreateRequest;
import com.example.chookjibupuser.api.review.dto.ReviewPageResponse;
import com.example.chookjibupuser.api.review.dto.ReviewResponse;
import com.example.chookjibupuser.application.review.UserReviewService;
import com.example.chookjibupuser.auth.support.UserPrincipal;
import com.example.chookjibupuser.global.response.ApiResponse;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 축제 리뷰(별점+한줄평) 작성/조회 API이다. QR코드/프론트 URL은 축제의 public_id(UUID)를
 * 담고 있고, 그 값을 그대로 경로에 쓴다 — festival_id(내부 PK)는 노출하지 않는다.
 *
 * <p>QR을 찍고 이 화면까지 들어오는 흐름 자체는 프론트 책임이고, 여기는 리뷰 작성/조회
 * API만 제공한다.</p>
 */
@Tag(name = "User Review", description = "축제 리뷰(별점+한줄평) API")
@RestController
@RequestMapping("/api/festivals/{festivalPublicId}/reviews")
@RequiredArgsConstructor
public class UserReviewController {

    private final UserReviewService userReviewService;

    @Operation(summary = "리뷰 작성", description = "별점(1~5)과 한줄평을 남깁니다. 로그인이 필요합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<ReviewResponse> createReview(
            @PathVariable UUID festivalPublicId,
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ReviewResponse response = userReviewService.createReview(
                festivalPublicId,
                requireUserId(principal),
                request
        );
        return ApiResponse.success(SuccessCode.REVIEW_CREATE_SUCCESS, response);
    }

    @Operation(summary = "리뷰 목록 조회", description = "비회원도 조회할 수 있습니다.")
    @GetMapping
    public ApiResponse<ReviewPageResponse> getReviews(
            @PathVariable UUID festivalPublicId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.success(
                SuccessCode.REVIEW_READ_SUCCESS,
                userReviewService.getReviews(festivalPublicId, page, size)
        );
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}
