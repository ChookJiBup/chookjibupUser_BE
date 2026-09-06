// api/congestion/UserCongestionController.java (신규)
package com.example.chookjibupuser.api.congestion;

import com.example.chookjibupuser.api.congestion.dto.FestivalCongestionResponse;
import com.example.chookjibupuser.application.congestion.UserCongestionService;
import com.example.chookjibupuser.global.response.ApiResponse;
import com.example.chookjibupuser.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 축제 현재 혼잡도 조회 API. 비회원도 볼 수 있다. 관리자/스태프가 관리자 백엔드에서
 * 갱신한 값을 그대로 읽기만 한다(이 서버는 쓰지 않음).
 */
@Tag(name = "User Congestion", description = "축제 혼잡도 조회 API (비회원 가능)")
@RestController
@RequestMapping("/api/festivals/{festivalPublicId}/congestion")
@RequiredArgsConstructor
public class UserCongestionController {

    private final UserCongestionService userCongestionService;

    @Operation(summary = "축제 혼잡도 조회", description = "부스별 최신 혼잡도와 대기시간 랭킹을 보여줍니다. "
            + "경로의 festivalPublicId는 목록/상세 조회 응답의 publicId 값입니다. "
            + "부스에 대해 아직 혼잡도가 한 번도 갱신 안 됐으면 해당 필드는 null입니다.")
    @GetMapping
    public ApiResponse<FestivalCongestionResponse> getCongestion(@PathVariable UUID festivalPublicId) {
        return ApiResponse.success(
                SuccessCode.FESTIVAL_CONGESTION_READ_SUCCESS,
                userCongestionService.getCongestion(festivalPublicId)
        );
    }
}