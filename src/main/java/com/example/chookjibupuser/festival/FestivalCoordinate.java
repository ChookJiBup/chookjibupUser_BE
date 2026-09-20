package com.example.chookjibupuser.festival;

import java.math.BigDecimal;

/**
 * 축제 한 건의 위경도.
 *
 * 둘 중 하나라도 비면 지도에 찍을 수 없으므로 «좌표 없음»으로 본다.
 */
public record FestivalCoordinate(BigDecimal latitude, BigDecimal longitude) {

    public static final FestivalCoordinate NONE = new FestivalCoordinate(null, null);

    public boolean isPresent() {
        return latitude != null && longitude != null;
    }
}
