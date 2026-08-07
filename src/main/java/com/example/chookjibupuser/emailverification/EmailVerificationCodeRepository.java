package com.example.chookjibupuser.emailverification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    /** 같은 이메일로 여러 번 코드를 재발급했을 수 있으니 가장 최근 것만 본다. */
    Optional<EmailVerificationCode> findTopByEmailOrderByVerificationIdDesc(String email);
}
