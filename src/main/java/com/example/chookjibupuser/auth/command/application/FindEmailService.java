package com.example.chookjibupuser.auth.command.application;

import com.example.chookjibupuser.api.auth.dto.FindEmailRequest;
import com.example.chookjibupuser.api.auth.dto.FindEmailResponse;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.user.UserAccount;
import com.example.chookjibupuser.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 아이디(이메일) 찾기 유스케이스를 처리한다. 이름(닉네임)+생년월일로 계정을 찾아서
 * 이메일 일부를 가린 채로 알려준다 — 이메일 전체를 그대로 보여주면 회원가입 폼의
 * "이름"란에 우연히 같은 값을 넣은 제3자가 계정 이메일을 그대로 알아낼 수 있어서,
 * 최소한의 노출로 줄였다.
 *
 * <p>카카오 로그인 계정은 "이메일+비밀번호"로 로그인하는 개념 자체가 없어서
 * (카카오 OAuth로만 로그인) 대상에서 제외한다 — EmailSignupRequest가 받는 생년월일과
 * 같은 포맷(연도 4자리/월일 4자리)으로 변환해서 비교한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindEmailService {

    // UserAccount.LOGIN_TYPE_EMAIL과 같은 값이다 — UserAccount는 이 상수를 private로
    // 감춰두고 isEmailLogin()만 공개하므로, 여기서는 리터럴을 그대로 쓴다.
    private static final String LOGIN_TYPE_EMAIL = "email";

    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_DAY_FORMAT = DateTimeFormatter.ofPattern("MMdd");

    private final UserAccountRepository userAccountRepository;

    public FindEmailResponse findEmail(FindEmailRequest request) {
        String birthyear = request.birthDate().format(YEAR_FORMAT);
        String birthday = request.birthDate().format(MONTH_DAY_FORMAT);

        List<UserAccount> matches = userAccountRepository
                .findByLoginTypeAndNicknameAndBirthyearAndBirthday(
                        LOGIN_TYPE_EMAIL, request.nickname(), birthyear, birthday
                );

        // 이름(닉네임)+생년월일만으로는 동명이인이 겹칠 수 있다. 정확히 1건일 때만
        // "찾았다"고 인정하고, 0건이든 여러 건이든 똑같이 USER_NOT_FOUND로 처리한다 —
        // "여러 명이 일치합니다"라고 따로 알려주면 그 자체로 "동명이인이 실제 가입돼
        // 있다"는 정보가 새어나가서, 못 찾은 경우와 구분 안 되게 응답을 통일했다.
        if (matches.size() != 1) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return new FindEmailResponse(maskEmail(matches.get(0).getEmail()));
    }

    /** "abcdefg@gmail.com" -> "abc****@gmail.com" (앞 3글자만 보여주고 나머지는 가림). */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        int visibleLength = Math.min(3, local.length());
        String visible = local.substring(0, visibleLength);
        String masked = "*".repeat(Math.max(local.length() - visibleLength, 3));
        return visible + masked + domain;
    }
}