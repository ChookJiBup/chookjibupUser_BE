package com.example.chookjibupuser.auth.command.infrastructure.kakao;

import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 카카오 인가 코드를 액세스 토큰으로 교환하고, 그 토큰으로 사용자 정보를 조회한다.
 * 표준 OAuth2 Authorization Code 흐름이다.
 */
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private static final String GRANT_TYPE = "authorization_code";

    private final RestClient kakaoRestClient;
    private final KakaoProperties kakaoProperties;

    /**
     * 인가 코드를 카카오 액세스 토큰으로 교환한다.
     *
     * @param code        프론트엔드가 카카오 로그인으로 받은 인가 코드
     * @param redirectUri 인가 코드를 발급받을 때 쓴 redirect_uri. null이면 기본값(app.kakao.redirect-uri) 사용
     */
    public KakaoTokenResponse exchangeToken(String code, String redirectUri) {
        if (!StringUtils.hasText(code)) {
            throw new CustomException(ErrorCode.AUTH_KAKAO_CODE_REQUIRED);
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", kakaoProperties.clientId());
        body.add("redirect_uri", resolveRedirectUri(redirectUri));
        body.add("code", code);
        if (StringUtils.hasText(kakaoProperties.clientSecret())) {
            body.add("client_secret", kakaoProperties.clientSecret());
        }

        try {
            KakaoTokenResponse response = kakaoRestClient.post()
                    .uri(TOKEN_URI)
                    .headers(headers -> headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .body(body)
                    .retrieve()
                    .body(KakaoTokenResponse.class);

            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new CustomException(ErrorCode.AUTH_KAKAO_LOGIN_FAILED);
            }
            return response;
        } catch (RestClientException exception) {
            throw new CustomException(ErrorCode.AUTH_KAKAO_LOGIN_FAILED);
        }
    }

    /**
     * 카카오 액세스 토큰으로 사용자 정보를 조회한다.
     */
    public KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
        try {
            KakaoUserInfoResponse response = kakaoRestClient.get()
                    .uri(USER_INFO_URI)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken))
                    .retrieve()
                    .body(KakaoUserInfoResponse.class);

            if (response == null || response.id() == null) {
                throw new CustomException(ErrorCode.AUTH_KAKAO_LOGIN_FAILED);
            }
            return response;
        } catch (RestClientException exception) {
            throw new CustomException(ErrorCode.AUTH_KAKAO_LOGIN_FAILED);
        }
    }

    private String resolveRedirectUri(String redirectUri) {
        return StringUtils.hasText(redirectUri) ? redirectUri : kakaoProperties.redirectUri();
    }
}
