package com.example.chookjibupuser.auth.command.infrastructure.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 사용자 정보 조회 API(https://kapi.kakao.com/v2/user/me) 응답이다.
 * 동의 항목에 따라 kakao_account 하위 필드가 없을 수 있다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserInfoResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAccount(
            @JsonProperty("email") String email,
            @JsonProperty("profile") KakaoProfile profile
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoProfile(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("profile_image_url") String profileImageUrl
    ) {
    }

    public String nicknameOrDefault() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return "카카오사용자";
        }
        String nickname = kakaoAccount.profile().nickname();
        return (nickname == null || nickname.isBlank()) ? "카카오사용자" : nickname;
    }

    public String emailOrNull() {
        return kakaoAccount == null ? null : kakaoAccount.email();
    }

    public String profileImageUrlOrNull() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().profileImageUrl();
    }
}
