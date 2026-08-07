package com.example.chookjibupuser.global.response;

import org.springframework.http.HttpStatus;

/**
 * API 실패 상황별 숫자 코드, HTTP 상태, 메시지를 정의한다.
 */
public enum ErrorCode {
    BAD_REQUEST(40000, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_REQUEST(40001, HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    AUTH_KAKAO_CODE_REQUIRED(40002, HttpStatus.BAD_REQUEST, "카카오 인가 코드가 필요합니다."),
    AUTH_PASSWORD_CONFIRM_MISMATCH(40003, HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    AUTH_EMAIL_VERIFICATION_CODE_MISMATCH(40004, HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    AUTH_EMAIL_VERIFICATION_EXPIRED(40005, HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다. 다시 요청해주세요."),
    AUTH_EMAIL_VERIFICATION_TOO_MANY_ATTEMPTS(40006, HttpStatus.BAD_REQUEST, "인증 시도 횟수를 초과했습니다. 다시 요청해주세요."),
    AUTH_EMAIL_NOT_VERIFIED(40007, HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),

    UNAUTHORIZED(40100, HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    AUTH_TOKEN_INVALID(40103, HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    AUTH_TOKEN_EXPIRED(40104, HttpStatus.UNAUTHORIZED, "만료된 인증 토큰입니다."),
    AUTH_KAKAO_LOGIN_FAILED(40105, HttpStatus.UNAUTHORIZED, "카카오 로그인에 실패했습니다."),
    AUTH_EMAIL_LOGIN_FAILED(40106, HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    FORBIDDEN(40300, HttpStatus.FORBIDDEN, "권한이 없습니다."),

    USER_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    FESTIVAL_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "축제를 찾을 수 없습니다."),
    AUTH_EMAIL_VERIFICATION_NOT_FOUND(40403, HttpStatus.NOT_FOUND, "인증번호를 먼저 요청해주세요."),

    AUTH_EMAIL_ALREADY_REGISTERED(40901, HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),

    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생하였습니다."),
    AUTH_EMAIL_SEND_FAILED(50001, HttpStatus.INTERNAL_SERVER_ERROR, "인증 메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
