package com.example.chookjibupuser.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** 아이디(이메일) 찾기 요청. 이름(닉네임)과 생년월일로 계정을 찾는다. */
public record FindEmailRequest(
        @NotBlank @Size(max = 100) String nickname,
        @NotNull LocalDate birthDate
) {
}