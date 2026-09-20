package com.example.chookjibupuser.festival;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 좌표를 어디서 가져올지 고르는 규칙을 고정한다.
 *
 * 관리자 콘솔이 등록한 축제는 좌표를 `festival_locations`에, 문화체육관광부 임포트
 * 축제는 `festivals` 컬럼에 둔다. 한쪽만 보면 반대쪽 축제가 지도에서 사라진다.
 */
class FestivalCoordinateResolverTest {

    private static Festival festival(Long id, String latitude, String longitude) {
        Festival festival = mock(Festival.class);
        when(festival.getFestivalId()).thenReturn(id);
        when(festival.getLatitude()).thenReturn(latitude == null ? null : new BigDecimal(latitude));
        when(festival.getLongitude()).thenReturn(longitude == null ? null : new BigDecimal(longitude));
        return festival;
    }

    private static FestivalPrimaryCoordinateRow row(
            Long festivalId,
            Long locationId,
            String latitude,
            String longitude
    ) {
        FestivalPrimaryCoordinateRow row = mock(FestivalPrimaryCoordinateRow.class);
        when(row.getFestivalId()).thenReturn(festivalId);
        when(row.getLocationId()).thenReturn(locationId);
        when(row.getLatitude()).thenReturn(new BigDecimal(latitude));
        when(row.getLongitude()).thenReturn(new BigDecimal(longitude));
        return row;
    }

    @Test
    @DisplayName("관리자가 등록한 축제는 대표 위치 좌표를 쓴다")
    void usesPrimaryLocationWhenFestivalRowHasNoCoordinate() {
        Festival adminCreated = festival(1L, null, null);

        Map<Long, FestivalCoordinate> resolved = FestivalCoordinateResolver.merge(
                List.of(adminCreated),
                List.of(row(1L, 10L, "36.5605070", "128.7306340"))
        );

        assertEquals(new BigDecimal("36.5605070"), resolved.get(1L).latitude());
        assertEquals(new BigDecimal("128.7306340"), resolved.get(1L).longitude());
        assertTrue(resolved.get(1L).isPresent());
    }

    @Test
    @DisplayName("대표 위치가 없는 임포트 축제는 축제 행의 좌표를 그대로 쓴다")
    void fallsBackToFestivalRowWhenNoPrimaryLocation() {
        Festival imported = festival(2L, "37.7725940", "128.9473210");

        Map<Long, FestivalCoordinate> resolved =
                FestivalCoordinateResolver.merge(List.of(imported), List.of());

        assertEquals(new BigDecimal("37.7725940"), resolved.get(2L).latitude());
        assertEquals(new BigDecimal("128.9473210"), resolved.get(2L).longitude());
    }

    @Test
    @DisplayName("양쪽 다 없으면 좌표 없음으로 둔다")
    void leavesCoordinateEmptyWhenNeitherSideHasOne() {
        Map<Long, FestivalCoordinate> resolved =
                FestivalCoordinateResolver.merge(List.of(festival(3L, null, null)), List.of());

        assertFalse(resolved.get(3L).isPresent());
    }

    @Test
    @DisplayName("한 목록에 섞여 있어도 축제마다 제 좌표를 찾아간다")
    void resolvesEachFestivalIndependently() {
        Map<Long, FestivalCoordinate> resolved = FestivalCoordinateResolver.merge(
                List.of(festival(1L, null, null), festival(2L, "37.0", "127.0"), festival(3L, null, null)),
                List.of(row(1L, 10L, "36.0", "128.0"))
        );

        assertEquals(new BigDecimal("36.0"), resolved.get(1L).latitude());
        assertEquals(new BigDecimal("37.0"), resolved.get(2L).latitude());
        assertFalse(resolved.get(3L).isPresent());
    }

    @Test
    @DisplayName("대표 위치가 여러 건이면 가장 먼저 만든 것을 쓴다")
    void picksTheOldestPrimaryLocationSoResultDoesNotWobble() {
        // 조회 순서가 뒤집혀 들어와도 결과가 같아야 한다.
        Map<Long, FestivalCoordinate> resolved = FestivalCoordinateResolver.merge(
                List.of(festival(1L, null, null)),
                List.of(row(1L, 20L, "35.0", "129.0"), row(1L, 10L, "36.0", "128.0"))
        );

        assertEquals(new BigDecimal("36.0"), resolved.get(1L).latitude());
    }

    @Test
    @DisplayName("좌표가 한쪽만 있으면 지도에 찍을 수 없으므로 없는 것으로 본다")
    void treatsHalfFilledCoordinateAsMissing() {
        Map<Long, FestivalCoordinate> resolved =
                FestivalCoordinateResolver.merge(List.of(festival(4L, "37.5", null)), List.of());

        assertFalse(resolved.get(4L).isPresent());
    }
}
