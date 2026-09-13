package com.example.chookjibupuser.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 비밀번호 재설정 확정 요청. 이메일 링크를 클릭하면 받은 token을 그대로 담아 보낸다. */
public record PasswordResetConfirmRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 64) String newPassword,
        @NotBlank String newPasswordConfirm
) {
}