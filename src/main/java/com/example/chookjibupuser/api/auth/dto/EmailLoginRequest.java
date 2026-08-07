package com.example.chookjibupuser.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 로그인 요청.
 */
public record EmailLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
