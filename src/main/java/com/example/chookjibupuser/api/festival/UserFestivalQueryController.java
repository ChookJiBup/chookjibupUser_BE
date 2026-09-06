// api/festival/UserFestivalQueryController.java (전체)
package com.example.chookjibupuser.api.festival;

import com.example.chookjibupuser.api.festival.dto.UserFestivalDetailResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalPageResponse;
import com.example.chookjibupuser.application.festival.UserFestivalService;
import com.example.chookjibupuser.auth.support.UserPrincipal;
import com.example.chookjibupuser.global.response.ApiResponse;
import com.example.chookjibupuser.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User Festival", description = "축제 목록 조회 API (비회원 가능)")
@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
public class UserFestivalQueryController {

    private final UserFestivalService userFestivalService;

    @Operation(summary = "축제 목록 조회", description = "비회원도 호출할 수 있습니다. "
            + "로그인 상태로 호출하면 항목마다 wishlisted 여부가 채워집니다. "
            + "name으로 축제명 부분 일치 검색이 가능합니다(대소문자 무시). "
            + "status(ONGOING/UPCOMING/COMPLETED)로 진행 상태 필터가 가능합니다. "
            + "sort(WISHLIST_COUNT/REVIEW_COUNT)로 찜/리뷰 많은 순 정렬이 가능합니다(기본은 시작일순). "
            + "name/status/sort는 서로 동시에 줄 수 없습니다. "
            + "[임시] region(지역) 필터는 아직 없습니다.")
    @GetMapping
    public ApiResponse<UserFestivalPageResponse> getFestivals(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserFestivalPageResponse response = userFestivalService.getFestivals(
                name,
                status,
                sort,
                page,
                size,
                principal == null ? null : principal.userId()
        );
        return ApiResponse.success(SuccessCode.FESTIVAL_LIST_READ_SUCCESS, response);
    }

    @Operation(summary = "축제 상세 조회", description = "비회원도 호출할 수 있습니다. "
            + "로그인 상태로 호출하면 wishlisted 여부가 채워집니다. "
            + "경로의 festivalPublicId는 목록 조회 응답의 publicId 값입니다.")
    @GetMapping("/{festivalPublicId}")
    public ApiResponse<UserFestivalDetailResponse> getFestival(
            @PathVariable UUID festivalPublicId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserFestivalDetailResponse response = userFestivalService.getFestivalDetail(
                festivalPublicId,
                principal == null ? null : principal.userId()
        );
        return ApiResponse.success(SuccessCode.FESTIVAL_LIST_READ_SUCCESS, response);
    }
}