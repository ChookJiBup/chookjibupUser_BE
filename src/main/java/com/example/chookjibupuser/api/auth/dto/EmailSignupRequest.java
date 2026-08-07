package com.example.chookjibupuser.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 이메일 회원가입 요청. 여기 들어오는 email은 이미 EmailVerificationConfirmRequest로
 * 인증이 끝난 상태여야 한다(EmailSignupService가 검증).
 */
public record EmailSignupRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank String passwordConfirm,
        @NotBlank @Size(max = 100) String nickname,
        String phoneNumber,
        // 생년월일(선택). 카카오 계정과 같은 저장 포맷(연도 4자리/월일 4자리)으로 변환해서 저장한다.
        LocalDate birthDate
) {
}
