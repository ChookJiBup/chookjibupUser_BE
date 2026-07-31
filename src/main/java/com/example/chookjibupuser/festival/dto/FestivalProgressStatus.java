package com.example.chookjibupuser.festival.dto;

/**
 * 축제 진행 상태이다. 예전에는 매 요청마다 오늘 날짜로 실시간 계산했지만,
 * 이제는 파이썬 파이프라인이 새벽 6시 배치(festival_status_updater.py)로
 * festivals.progress_status 컬럼에 미리 계산해서 채워둔 값을 그대로 읽기만 한다.
 *
 * DB 컬럼값(Postgres 네이티브 ENUM)은 소문자 문자열("upcoming"/"ongoing"/"completed")이고,
 * API 쪽 계약(쿼리 파라미터/응답 필드)은 그대로 대문자를 유지하기 위해 이 안에서 변환한다.
 */
public enum FestivalProgressStatus {
    UPCOMING,
    ONGOING,
    COMPLETED;

    /** DB에 넘길 값 (소문자). WHERE 절 파라미터로 쓴다. */
    public String toDbValue() {
        return name().toLowerCase();
    }

    /** DB에서 읽은 값(소문자, null 가능)을 이 enum으로 변환한다. */
    public static FestivalProgressStatus fromDbValue(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }
        return FestivalProgressStatus.valueOf(dbValue.trim().toUpperCase());
    }
}
