package com.example.chookjibupuser.auth.command.application;

import com.example.chookjibupuser.api.auth.dto.EmailLoginRequest;
import com.example.chookjibupuser.api.auth.dto.UserLoginResponse;
import com.example.chookjibupuser.auth.command.infrastructure.JwtTokenProvider;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.user.UserAccount;
import com.example.chookjibupuser.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이메일/비밀번호 로그인 유스케이스를 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailLoginService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserLoginResponse login(EmailLoginRequest request) {
        UserAccount userAccount = userAccountRepository.findByEmail(request.email())
                // 이메일이 아예 없는 경우와 비밀번호가 틀린 경우를 같은 에러로 묶는다 —
                // "이 이메일은 가입 안 됨"을 알려주면 계정 존재 여부가 노출되기 때문이다.
                .filter(UserAccount::isEmailLogin)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_EMAIL_LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), userAccount.getPasswordHash())) {
            throw new CustomException(ErrorCode.AUTH_EMAIL_LOGIN_FAILED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(userAccount);
        return UserLoginResponse.of(
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                false,
                userAccount
        );
    }
}
