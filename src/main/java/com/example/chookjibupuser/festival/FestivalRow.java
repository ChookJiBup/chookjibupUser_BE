package com.example.chookjibupuser.festival;

import java.time.LocalDate;

/**
 * 네이티브 쿼리 결과를 받는 인터페이스 프로젝션이다.
 *
 * <p>{@code progress_status} 컬럼은 Postgres 네이티브 ENUM 타입이라({@code festival_progress_status}),
 * {@link Festival} 엔티티 필드로 직접 매핑하지 않았다 — congestion 도메인에서 같은 이유로
 * {@code congestion_level}을 매핑하지 않은 것과 동일한 이유다 (JDBC가 이 컬럼을 VARCHAR가 아니라
 * OTHER/사용자 정의 타입으로 보고해서, 일반 매핑은 {@code ddl-auto: validate}에서 타입 불일치로
 * 실패할 위험이 크다). 대신 {@link FestivalRepository}의 네이티브 쿼리에서 {@code ::text}로
 * 캐스팅해서 문자열(소문자: upcoming/ongoing/completed)로 받는다.</p>
 */
public interface FestivalRow {

    Long getFestivalId();

    String getFestivalName();

    String getEventPlace();

    String getRoadAddress();

    LocalDate getStartDate();

    LocalDate getEndDate();

    String getContent();

    String getPhoneNumber();

    String getHomepageUrl();

    /** 파이프라인이 새벽 6시 배치로 채워둔 값. 소문자 문자열("upcoming"/"ongoing"/"completed") 또는 null. */
    String getProgressStatus();
}
