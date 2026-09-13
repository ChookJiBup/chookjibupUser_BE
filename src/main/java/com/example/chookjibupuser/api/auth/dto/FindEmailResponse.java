package com.example.chookjibupuser.api.auth.dto;

/** {@code maskedEmail}은 개인정보 노출을 줄이기 위해 일부를 *로 가린 이메일이다(예: ab***@gmail.com). */
public record FindEmailResponse(
        String maskedEmail
) {
}