package com.example.chookjibupuser.global.response;

import org.springframework.http.HttpStatus;

/**
 * API 실패 상황별 숫자 코드, HTTP 상태, 메시지를 정의한다.
 */
public enum ErrorCode {
    BAD_REQUEST(40000, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_REQUEST(40001, HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    AUTH_KAKAO_CODE_REQUIRED(40002, HttpStatus.BAD_REQUEST, "카카오 인가 코드가 필요합니다."),

    UNAUTHORIZED(40100, HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    AUTH_TOKEN_INVALID(40103, HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    AUTH_TOKEN_EXPIRED(40104, HttpStatus.UNAUTHORIZED, "만료된 인증 토큰입니다."),
    AUTH_KAKAO_LOGIN_FAILED(40105, HttpStatus.UNAUTHORIZED, "카카오 로그인에 실패했습니다."),

    FORBIDDEN(40300, HttpStatus.FORBIDDEN, "권한이 없습니다."),

    USER_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    FESTIVAL_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "축제를 찾을 수 없습니다."),

    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생하였습니다.");

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
