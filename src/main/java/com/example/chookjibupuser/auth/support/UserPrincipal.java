package com.example.chookjibupuser.auth.support;

/**
 * JWT 인증 후 SecurityContext에 저장되는 사용자 인증 주체이다.
 */
public record UserPrincipal(
        Long userId,
        String nickname
) {
}
