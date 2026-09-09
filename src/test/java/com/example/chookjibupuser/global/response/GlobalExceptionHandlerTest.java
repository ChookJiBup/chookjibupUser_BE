package com.example.chookjibupuser.global.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /**
     * 매핑된 핸들러가 없는 경로는 404다. 예전에는 catch-all이 받아 500을 내려줘서,
     * 프론트가 쓰지도 않는 오타 경로가 서버 장애처럼 보였다.
     */
    @Test
    void 없는_경로는_404를_내려준다() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleNoResourceFoundException();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ErrorCode.NOT_FOUND.getCode(), response.getBody().code());
    }

    @Test
    void 처리하지_못한_예외는_500을_내려준다() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/festivals");

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleException(request, new IllegalStateException("예상치 못한 오류"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getBody().code());
    }
}
