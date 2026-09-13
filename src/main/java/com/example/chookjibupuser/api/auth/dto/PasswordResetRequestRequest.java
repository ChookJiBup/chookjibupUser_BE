package com.example.chookjibupuser.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 비밀번호 재설정 링크 발송 요청. */
public record PasswordResetRequestRequest(
        @NotBlank @Email String email
) {
}