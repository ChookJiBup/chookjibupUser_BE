package com.example.chookjibupuser.global.response;

/**
 * API 성공과 실패 응답을 동일한 형태로 감싸는 공통 응답 모델이다.
 */
public record ApiResponse<T>(
        int code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(successCode.getCode(), successCode.getMessage(), data);
    }

    public static ApiResponse<Void> success(SuccessCode successCode) {
        return new ApiResponse<>(successCode.getCode(), successCode.getMessage(), null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
