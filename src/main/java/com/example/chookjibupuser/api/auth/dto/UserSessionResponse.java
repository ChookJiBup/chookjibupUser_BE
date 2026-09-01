package com.example.chookjibupuser.api.auth.dto;

import com.example.chookjibupuser.user.UserAccount;

/** HttpOnly 쿠키의 토큰을 노출하지 않고 프론트엔드에 세션 정보만 제공한다. */
public record UserSessionResponse(
        long expiresIn,
        boolean newUser,
        String nickname,
        String email,
        String profileImageUrl
) {
    public static UserSessionResponse from(UserLoginResponse response) {
        return new UserSessionResponse(
                response.accessTokenExpiresInSeconds(),
                response.newUser(),
                response.nickname(),
                response.email(),
                response.profileImageUrl()
        );
    }

    public static UserSessionResponse from(UserAccount userAccount, long expiresIn) {
        return new UserSessionResponse(
                expiresIn,
                false,
                userAccount.getNickname(),
                userAccount.getEmail(),
                userAccount.getProfileImageUrl()
        );
    }
}
