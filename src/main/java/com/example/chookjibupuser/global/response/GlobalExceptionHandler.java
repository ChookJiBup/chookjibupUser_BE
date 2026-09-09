package com.example.chookjibupuser.global.response;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 전역 예외를 표준 ApiResponse 에러 응답으로 변환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException() {
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException() {
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST));
    }

    /**
     * 매핑된 핸들러가 없는 경로. 없는 경로는 404다.
     *
     * <p>이 핸들러가 없으면 아래 {@link #handleException}이 대신 받아 500을 내려준다.
     * 실제로 프론트가 쓰지도 않는 {@code /api/festivals/{id}/roadmap} 같은 오타 경로가
     * 모든 축제에서 500으로 보여서, 서버가 죽은 것처럼 오해하게 만들었다.</p>
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException() {
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.NOT_FOUND));
    }

    /**
     * 어디서도 처리하지 못한 예외.
     *
     * <p>반드시 로그를 남긴다. 예전에는 조용히 500만 내려줘서, 축제 3개의 상세 API가 며칠 동안
     * 500이었는데도 서버 로그에 아무 흔적이 없었다. 원인을 좁힐 단서가 응답 본문뿐이면
     * 이런 장애는 다시 묻힌다.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            HttpServletRequest request,
            Exception exception
    ) {
        log.error("처리하지 못한 예외가 발생했습니다. method={}, uri={}",
                request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
