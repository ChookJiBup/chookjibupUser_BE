package com.example.chookjibupuser.api.wishlist;

import com.example.chookjibupuser.api.wishlist.dto.MyWishlistPageResponse;
import com.example.chookjibupuser.api.wishlist.dto.WishlistToggleResponse;
import com.example.chookjibupuser.application.wishlist.UserWishlistService;
import com.example.chookjibupuser.auth.support.UserPrincipal;
import com.example.chookjibupuser.global.response.ApiResponse;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 축제 찜 토글/내 찜 목록 조회 API. 로그인한 사용자만 사용할 수 있다.
 * 컨트롤러는 UserWishlistService 호출과 인증 주체 추출만 한다.
 */
@Tag(name = "User Wishlist", description = "축제 찜 API (로그인 필요)")
@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class UserWishlistController {

    private final UserWishlistService userWishlistService;

    /**
     * 하트 아이콘 클릭에 매핑되는 API이다. 찜 안 했으면 찜하고, 이미 찜했으면
     * 취소한다 — 둘 다 정상 동작이라 실패 응답이 없다. 응답의 wishlisted 값으로
     * 최종 상태만 확인하면 된다.
     */
    @Operation(summary = "축제 찜 토글 (하트 클릭)", description = "찜 안 한 상태에서 누르면 찜하고, "
            + "이미 찜한 상태에서 누르면 찜을 취소합니다. 실패 케이스 없이 항상 최종 상태를 돌려줍니다. "
            + "경로의 festivalPublicId는 목록/상세 조회 응답의 publicId 값입니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{festivalPublicId}/toggle")
    public ApiResponse<WishlistToggleResponse> toggle(
            @PathVariable UUID festivalPublicId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WishlistToggleResponse response = userWishlistService.toggle(requireUserId(principal), festivalPublicId);
        return ApiResponse.success(SuccessCode.WISHLIST_TOGGLE_SUCCESS, response);
    }

    @Operation(summary = "내 찜 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ApiResponse<MyWishlistPageResponse> getMyWishlist(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success(
                SuccessCode.WISHLIST_READ_SUCCESS,
                userWishlistService.getMyWishlist(requireUserId(principal), page, size)
        );
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}
