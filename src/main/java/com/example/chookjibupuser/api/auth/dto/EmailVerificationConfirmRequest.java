package com.example.chookjibupuser.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 인증코드 확인 요청.
 */
public record EmailVerificationConfirmRequest(
        @NotBlank @Email String email,
        @NotBlank String code
) {
}
