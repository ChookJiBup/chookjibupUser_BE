package com.example.chookjibupuser.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 카카오 로그인 요청. 프론트엔드에서 받은 카카오 인가 코드를 담는다.
 */
public record KakaoLoginRequest(
        @NotBlank String code,
        // 인가 코드를 발급받을 때 쓴 redirect_uri. 생략하면 서버 기본값(app.kakao.redirect-uri) 사용
        String redirectUri
) {
}
