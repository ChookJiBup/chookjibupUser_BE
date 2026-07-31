package com.example.chookjibupuser.api.auth;

import com.example.chookjibupuser.api.auth.dto.KakaoLoginRequest;
import com.example.chookjibupuser.api.auth.dto.UserLoginResponse;
import com.example.chookjibupuser.auth.command.application.KakaoLoginService;
import com.example.chookjibupuser.global.response.ApiResponse;
import com.example.chookjibupuser.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카카오 로그인 API를 제공한다. 최초 로그인 시 자동으로 회원가입된다.
 */
@Tag(name = "User Auth", description = "카카오 로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final KakaoLoginService kakaoLoginService;

    @Operation(summary = "카카오 로그인", description = "프론트엔드에서 받은 카카오 인가 코드로 로그인합니다. "
            + "가입 이력이 없으면 자동으로 회원가입됩니다.")
    @PostMapping("/kakao/login")
    public ApiResponse<UserLoginResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ApiResponse.success(SuccessCode.USER_KAKAO_LOGIN_SUCCESS, kakaoLoginService.login(request));
    }
}
