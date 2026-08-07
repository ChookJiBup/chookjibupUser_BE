package com.example.chookjibupuser.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 인증코드 발송 요청.
 */
public record EmailVerificationRequest(
        @NotBlank @Email String email
) {
}
