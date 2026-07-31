package com.example.chookjibupuser.auth.command.application;

import com.example.chookjibupuser.api.auth.dto.KakaoLoginRequest;
import com.example.chookjibupuser.api.auth.dto.UserLoginResponse;
import com.example.chookjibupuser.auth.command.infrastructure.JwtTokenProvider;
import com.example.chookjibupuser.auth.command.infrastructure.kakao.KakaoOAuthClient;
import com.example.chookjibupuser.auth.command.infrastructure.kakao.KakaoTokenResponse;
import com.example.chookjibupuser.auth.command.infrastructure.kakao.KakaoUserInfoResponse;
import com.example.chookjibupuser.user.UserAccount;
import com.example.chookjibupuser.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카카오 인가 코드로 로그인(최초 로그인 시 자동 회원가입)하는 유스케이스를 처리한다.
 */
@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserAccountRepository userAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserLoginResponse login(KakaoLoginRequest request) {
        KakaoTokenResponse token = kakaoOAuthClient.exchangeToken(request.code(), request.redirectUri());
        KakaoUserInfoResponse kakaoUserInfo = kakaoOAuthClient.getUserInfo(token.accessToken());

        UserAccount existing = userAccountRepository.findByKakaoId(kakaoUserInfo.id()).orElse(null);
        boolean newUser = existing == null;

        UserAccount userAccount = newUser
                ? userAccountRepository.save(UserAccount.createFromKakao(
                        kakaoUserInfo.id(),
                        kakaoUserInfo.nicknameOrDefault(),
                        kakaoUserInfo.emailOrNull(),
                        kakaoUserInfo.profileImageUrlOrNull()
                ))
                : syncProfile(existing, kakaoUserInfo);

        String accessToken = jwtTokenProvider.createAccessToken(userAccount);

        return UserLoginResponse.of(
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                newUser,
                userAccount
        );
    }

    private UserAccount syncProfile(UserAccount userAccount, KakaoUserInfoResponse kakaoUserInfo) {
        userAccount.syncKakaoProfile(
                kakaoUserInfo.nicknameOrDefault(),
                kakaoUserInfo.emailOrNull(),
                kakaoUserInfo.profileImageUrlOrNull()
        );
        return userAccount;
    }
}
