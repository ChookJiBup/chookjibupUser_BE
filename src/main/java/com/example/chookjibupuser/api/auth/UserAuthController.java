package com.example.chookjibupuser.api.auth;

import com.example.chookjibupuser.api.auth.dto.*;
import com.example.chookjibupuser.auth.command.application.EmailLoginService;
import com.example.chookjibupuser.auth.command.application.EmailSignupService;
import com.example.chookjibupuser.auth.command.application.EmailVerificationRequestService;
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
 * 카카오 로그인, 이메일 회원가입/로그인 API를 제공한다.
 * 이메일 가입 순서: 인증코드 발송 → 인증코드 확인 → 회원가입.
 */
@Tag(name = "User Auth", description = "카카오/이메일 로그인·회원가입 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final KakaoLoginService kakaoLoginService;
    private final EmailVerificationRequestService emailVerificationRequestService;
    private final EmailSignupService emailSignupService;
    private final EmailLoginService emailLoginService;

    @Operation(summary = "카카오 로그인", description = "프론트엔드에서 받은 카카오 인가 코드로 로그인합니다. "
            + "가입 이력이 없으면 자동으로 회원가입됩니다.")
    @PostMapping("/kakao/login")
    public ApiResponse<UserLoginResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ApiResponse.success(SuccessCode.USER_KAKAO_LOGIN_SUCCESS, kakaoLoginService.login(request));
    }

    @Operation(summary = "이메일 인증코드 발송", description = "회원가입할 이메일로 6자리 인증코드를 보냅니다. "
            + "코드는 15분간 유효합니다.")
    @PostMapping("/email/verification-code")
    public ApiResponse<Void> sendVerificationCode(@Valid @RequestBody EmailVerificationRequest request) {
        emailVerificationRequestService.sendVerificationCode(request.email());
        return ApiResponse.success(SuccessCode.AUTH_EMAIL_VERIFICATION_CODE_SENT);
    }

    @Operation(summary = "이메일 인증코드 확인", description = "발송된 인증코드가 맞는지 확인합니다. "
            + "성공하면 이 이메일로 회원가입을 진행할 수 있습니다.")
    @PostMapping("/email/verification-code/confirm")
    public ApiResponse<Void> confirmVerificationCode(@Valid @RequestBody EmailVerificationConfirmRequest request) {
        emailVerificationRequestService.confirmVerificationCode(request.email(), request.code());
        return ApiResponse.success(SuccessCode.AUTH_EMAIL_VERIFICATION_SUCCESS);
    }

    @Operation(summary = "이메일 회원가입", description = "인증코드 확인이 먼저 완료된 이메일만 가입할 수 있습니다.")
    @PostMapping("/email/signup")
    public ApiResponse<UserLoginResponse> emailSignup(@Valid @RequestBody EmailSignupRequest request) {
        return ApiResponse.success(SuccessCode.USER_EMAIL_SIGNUP_SUCCESS, emailSignupService.signup(request));
    }

    @Operation(summary = "이메일 로그인")
    @PostMapping("/email/login")
    public ApiResponse<UserLoginResponse> emailLogin(@Valid @RequestBody EmailLoginRequest request) {
        return ApiResponse.success(SuccessCode.USER_EMAIL_LOGIN_SUCCESS, emailLoginService.login(request));
    }
}
