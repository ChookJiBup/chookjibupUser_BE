package com.example.chookjibupuser.festival;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 축제 진행 상태의 기준일은 서버 기본 시간대가 아니라 한국 날짜여야 한다.
 *
 * <p>운영 서버(EC2)의 기본 시간대는 UTC라서, 한국시간 00:00~09:00 사이에는 UTC 날짜가
 * 하루 전이다. 예전에는 {@code LocalDate.now()}를 그대로 써서 이 시간대에 오늘 시작한
 * 축제가 방문객 앱에서만 "진행 예정"으로 내려갔고, 같은 축제가 관리자 화면(Asia/Seoul
 * 시계 사용)에서는 "진행중"으로 보였다.
 */
class FestivalProgressStatusTimeZoneTest {

    /** 2026-09-09 02:27 KST == 2026-09-08 17:27 UTC — 실제로 불일치가 관측된 시각대. */
    private static final Instant EARLY_MORNING_IN_KOREA = Instant.parse("2026-09-08T17:27:00Z");

    @Test
    void treatsAFestivalStartingTodayInKoreaAsOngoingEvenBeforeUtcMidnightRollover() {
        FestivalQueryService service = serviceWithFixedClock(
                festivalRunning(LocalDate.of(2026, 9, 9), LocalDate.of(2026, 9, 10))
        );

        FestivalDetailView detail = service.getFestival(1L);

        assertEquals(FestivalProgressStatus.ONGOING, detail.progressStatus());
    }

    @Test
    void treatsAFestivalThatEndedYesterdayInKoreaAsCompleted() {
        FestivalQueryService service = serviceWithFixedClock(
                festivalRunning(LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 8))
        );

        FestivalDetailView detail = service.getFestival(1L);

        assertEquals(FestivalProgressStatus.COMPLETED, detail.progressStatus());
    }

    @Test
    void treatsAFestivalStartingTomorrowInKoreaAsUpcoming() {
        FestivalQueryService service = serviceWithFixedClock(
                festivalRunning(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 11))
        );

        FestivalDetailView detail = service.getFestival(1L);

        assertEquals(FestivalProgressStatus.UPCOMING, detail.progressStatus());
    }

    private FestivalQueryService serviceWithFixedClock(Festival festival) {
        FestivalRepository repository = mock(FestivalRepository.class);
        when(repository.findById(any())).thenReturn(Optional.of(festival));
        when(repository.findByFestivalIdIn(anyList())).thenReturn(List.of(festival));
        Clock clock = Clock.fixed(EARLY_MORNING_IN_KOREA, ZoneId.of("Asia/Seoul"));
        return new FestivalQueryService(repository, clock);
    }

    private Festival festivalRunning(LocalDate startDate, LocalDate endDate) {
        Festival festival = mock(Festival.class);
        when(festival.getFestivalId()).thenReturn(1L);
        when(festival.getStartDate()).thenReturn(startDate);
        when(festival.getEndDate()).thenReturn(endDate);
        return festival;
    }
}
