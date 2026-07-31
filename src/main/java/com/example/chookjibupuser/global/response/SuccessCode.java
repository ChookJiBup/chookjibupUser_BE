package com.example.chookjibupuser.global.response;

/**
 * API 성공 상황별 숫자 코드와 메시지를 정의한다.
 */
public enum SuccessCode {
    OK(20000, "요청이 성공적으로 처리되었습니다."),
    USER_KAKAO_LOGIN_SUCCESS(21000, "카카오 로그인에 성공했습니다."),
    FESTIVAL_LIST_READ_SUCCESS(22000, "축제 목록 조회가 완료되었습니다."),
    WISHLIST_TOGGLE_SUCCESS(23000, "찜 상태가 변경되었습니다."),
    WISHLIST_READ_SUCCESS(23002, "찜한 축제 목록 조회가 완료되었습니다.");

    private final int code;
    private final String message;

    SuccessCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
