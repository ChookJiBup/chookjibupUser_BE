package com.example.chookjibupuser.api.review;

import com.example.chookjibupuser.api.review.dto.MyReviewPageResponse;
import com.example.chookjibupuser.api.review.dto.ReviewResponse;
import com.example.chookjibupuser.api.review.dto.ReviewUpdateRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마이페이지 "내가 쓴 리뷰" 목록 조회 + 본인 리뷰 수정/삭제 API. 로그인한 사용자만
 * 사용할 수 있고, 수정/삭제는 본인이 작성한 리뷰가 아니면 거부된다(FORBIDDEN).
 *
 * <p>리뷰 수정/삭제는 reviewId(내부 PK) 하나로 대상이 정확히 특정되므로
 * festivalPublicId 경로가 굳이 필요 없다 — 그래서 {@code /api/reviews/{reviewId}}로 뒀다.</p>
 */
@Tag(name = "User Review", description = "축제 리뷰(별점+한줄평) API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class UserMyReviewController {

    private final UserReviewService userReviewService;

    @Operation(summary = "내가 쓴 리뷰 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ApiResponse<MyReviewPageResponse> getMyReviews(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success(
                SuccessCode.MY_REVIEW_READ_SUCCESS,
                userReviewService.getMyReviews(requireUserId(principal), page, size)
        );
    }

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰의 별점/한줄평을 수정합니다. "
            + "본인 리뷰가 아니면 403(FORBIDDEN), 존재하지 않는 리뷰면 404(REVIEW_NOT_FOUND)를 반환합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success(
                SuccessCode.REVIEW_UPDATE_SUCCESS,
                userReviewService.updateReview(requireUserId(principal), reviewId, request)
        );
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다. "
            + "본인 리뷰가 아니면 403(FORBIDDEN), 존재하지 않는 리뷰면 404(REVIEW_NOT_FOUND)를 반환합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userReviewService.deleteReview(requireUserId(principal), reviewId);
        return ApiResponse.success(SuccessCode.REVIEW_DELETE_SUCCESS);
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}