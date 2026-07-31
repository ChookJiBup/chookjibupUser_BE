package com.example.chookjibupuser.congestion;

import java.time.OffsetDateTime;

/**
 * 네이티브 쿼리 결과를 받는 인터페이스 프로젝션이다. Spring Data가 SELECT 별칭
 * (AS boothId 등)과 getter 이름을 매칭해서 자동으로 채워준다.
 */
public interface BoothCongestionProjection {

    Long getBoothId();

    String getCongestionLevel();

    Integer getWaitMinutes();

    OffsetDateTime getUpdatedAt();
}
