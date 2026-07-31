package com.example.chookjibupuser.api.auth.dto;

import com.example.chookjibupuser.user.UserAccount;

/**
 * 카카오 로그인 성공 응답이다.
 */
public record UserLoginResponse(
        String accessToken,
        long accessTokenExpiresInSeconds,
        boolean newUser,
        String nickname,
        String email,
        String profileImageUrl
) {

    public static UserLoginResponse of(
            String accessToken,
            long accessTokenExpiresInSeconds,
            boolean newUser,
            UserAccount userAccount
    ) {
        return new UserLoginResponse(
                accessToken,
                accessTokenExpiresInSeconds,
                newUser,
                userAccount.getNickname(),
                userAccount.getEmail(),
                userAccount.getProfileImageUrl()
        );
    }
}
