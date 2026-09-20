package com.example.chookjibupuser.festival;

import java.math.BigDecimal;

/** `festival_locations`에서 대표 위치 좌표만 꺼내 오는 조회 전용 투영. */
public interface FestivalPrimaryCoordinateRow {

    Long getFestivalId();

    /** 대표 위치를 여러 건 둔 축제가 생기면 가장 먼저 만든 것을 고르는 데 쓴다. */
    Long getLocationId();

    BigDecimal getLatitude();

    BigDecimal getLongitude();
}
