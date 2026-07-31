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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 축제 목록 조회 API. 비회원도 조회할 수 있다.
 * 컨트롤러는 UserFestivalService 호출과 인증 주체 추출만 하고, 실제 조회/조합
 * 로직은 전부 서비스에 있다.
 */
@Tag(name = "User Festival", description = "축제 목록 조회 API (비회원 가능)")
@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
public class UserFestivalQueryController {

    private final UserFestivalService userFestivalService;

    @Operation(summary = "축제 목록 조회", description = "비회원도 호출할 수 있습니다. "
            + "로그인 상태로 호출하면 항목마다 wishlisted 여부가 채워집니다. "
            + "[임시] 필터(상태/이름/지역)는 서버 에러 원인 파악 전까지 잠시 뺐습니다 — 페이지네이션만 지원합니다.")
    @GetMapping
    public ApiResponse<UserFestivalPageResponse> getFestivals(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserFestivalPageResponse response = userFestivalService.getFestivals(
                page,
                size,
                principal == null ? null : principal.userId()
        );
        return ApiResponse.success(SuccessCode.FESTIVAL_LIST_READ_SUCCESS, response);
    }

    @Operation(summary = "축제 상세 조회", description = "비회원도 호출할 수 있습니다. "
            + "로그인 상태로 호출하면 wishlisted 여부가 채워집니다.")
    @GetMapping("/{festivalId}")
    public ApiResponse<UserFestivalDetailResponse> getFestival(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserFestivalDetailResponse response = userFestivalService.getFestivalDetail(
                festivalId,
                principal == null ? null : principal.userId()
        );
        return ApiResponse.success(SuccessCode.FESTIVAL_LIST_READ_SUCCESS, response);
    }
}
