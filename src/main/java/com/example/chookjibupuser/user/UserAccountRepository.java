package com.example.chookjibupuser.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByKakaoId(Long kakaoId);

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserAccount> findByUserIdIn(List<Long> userIds);

    /**
     * 아이디(이메일) 찾기에서 쓴다. 카카오 계정은 "이메일+비밀번호"로 로그인하는 개념
     * 자체가 없어서(카카오 OAuth로만 로그인) loginType="email"인 것만 대상으로 한다.
     *
     * <p>[중요] Optional이 아니라 List로 받는다 — 이름(닉네임)+생년월일만으로는 동명이인이
     * 겹칠 수 있어서(실제로 겹쳐서 NonUniqueResultException이 난 적 있음), 여러 건이
     * 나올 가능성을 정상 케이스로 다뤄야 한다. 몇 건이 나오든 여기선 그냥 다 돌려주고,
     * "정확히 1건일 때만 찾은 것으로 인정"하는 판단은 FindEmailService가 한다.</p>
     */
    List<UserAccount> findByLoginTypeAndNicknameAndBirthyearAndBirthday(
            String loginType, String nickname, String birthyear, String birthday
    );
}