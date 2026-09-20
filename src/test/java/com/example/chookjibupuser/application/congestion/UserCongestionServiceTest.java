package com.example.chookjibupuser.application.congestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.chookjibupuser.api.congestion.dto.BoothCongestionResponse;
import com.example.chookjibupuser.api.congestion.dto.FestivalCongestionResponse;
import com.example.chookjibupuser.booth.BoothQueryService;
import com.example.chookjibupuser.booth.dto.BoothView;
import com.example.chookjibupuser.congestion.BoothCongestionLevel;
import com.example.chookjibupuser.congestion.CongestionQueryService;
import com.example.chookjibupuser.congestion.dto.BoothCongestionView;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.roadmap.RoadmapQueryService;
import com.example.chookjibupuser.roadmap.dto.BoothZoneView;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * 축제 단위 혼잡도 등급과 구역이 응답에 실리는지 고정한다.
 *
 * <p>등급 규칙(부스 등급 중 최고값)은 프런트가 서버에 필드가 없던 동안 쓰던 규칙과
 * 같아야 한다 — 달라지면 배포와 동시에 방문객 화면의 「전체 혼잡도」가 바뀐다.</p>
 */
class UserCongestionServiceTest {

    private static final UUID FESTIVAL_PUBLIC_ID = UUID.randomUUID();
    private static final long FESTIVAL_ID = 7L;

    private final FestivalQueryService festivalQueryService = mock(FestivalQueryService.class);
    private final BoothQueryService boothQueryService = mock(BoothQueryService.class);
    private final CongestionQueryService congestionQueryService = mock(CongestionQueryService.class);
    private final RoadmapQueryService roadmapQueryService = mock(RoadmapQueryService.class);

    private final UserCongestionService userCongestionService = new UserCongestionService(
            festivalQueryService,
            boothQueryService,
            congestionQueryService,
            roadmapQueryService
    );

    @Test
    void 부스가_없으면_축제_혼잡도는_없다() {
        givenBooths(List.of());
        givenCongestions(List.of());
        givenZones(List.of());

        FestivalCongestionResponse response = userCongestionService.getCongestion(FESTIVAL_PUBLIC_ID);

        assertNull(response.congestionLevel());
        assertNull(response.activeQueueCount());
        assertEquals(List.of(), response.booths());
    }

    @Test
    void 아직_아무도_갱신하지_않았으면_축제_혼잡도는_없다() {
        givenBooths(List.of(booth(1L, "커피", 11L), booth(2L, "붕어빵", 12L)));
        givenCongestions(List.of());
        givenZones(List.of());

        assertNull(userCongestionService.getCongestion(FESTIVAL_PUBLIC_ID).congestionLevel());
    }

    @Test
    void 한_등급만_있으면_그_등급이_축제_혼잡도다() {
        givenBooths(List.of(booth(1L, "커피", 11L), booth(2L, "붕어빵", 12L)));
        givenCongestions(List.of(
                congestion(1L, BoothCongestionLevel.MEDIUM, 10),
                congestion(2L, BoothCongestionLevel.MEDIUM, 20)
        ));
        givenZones(List.of());

        assertEquals(
                BoothCongestionLevel.MEDIUM,
                userCongestionService.getCongestion(FESTIVAL_PUBLIC_ID).congestionLevel()
        );
    }

    @Test
    void 등급이_섞여_있으면_가장_높은_등급이_축제_혼잡도다() {
        givenBooths(List.of(booth(1L, "커피", 11L), booth(2L, "붕어빵", 12L), booth(3L, "솜사탕", 13L)));
        givenCongestions(List.of(
                congestion(1L, BoothCongestionLevel.LOW, 1),
                congestion(2L, BoothCongestionLevel.HIGH, 40),
                congestion(3L, BoothCongestionLevel.MEDIUM, 15)
        ));
        givenZones(List.of());

        assertEquals(
                BoothCongestionLevel.HIGH,
                userCongestionService.getCongestion(FESTIVAL_PUBLIC_ID).congestionLevel()
        );
    }

    /** 대기시간이 없어 랭킹에서 빠진 부스도 등급은 있을 수 있다 — 전체 혼잡도는 그것까지 본다. */
    @Test
    void 대기시간이_없어_랭킹에서_빠진_부스의_등급도_축제_혼잡도에_반영된다() {
        givenBooths(List.of(booth(1L, "커피", 11L)));
        givenCongestions(List.of(congestion(1L, BoothCongestionLevel.HIGH, null)));
        givenZones(List.of());

        FestivalCongestionResponse response = userCongestionService.getCongestion(FESTIVAL_PUBLIC_ID);

        assertEquals(List.of(), response.ranking());
        assertEquals(BoothCongestionLevel.HIGH, response.congestionLevel());
    }

    @Test
    void 부스에_구역과_지도_노드가_붙는다() {
        UUID zoneId = UUID.randomUUID();
        UUID nodePublicId = UUID.randomUUID();
        givenBooths(List.of(booth(1L, "커피", 11L)));
        givenCongestions(List.of(congestion(1L, BoothCongestionLevel.LOW, 3)));
        givenZones(List.of(new BoothZoneView(11L, nodePublicId, zoneId, "먹거리존")));

        BoothCongestionResponse item = userCongestionService
                .getCongestion(FESTIVAL_PUBLIC_ID).booths().get(0);

        assertEquals(nodePublicId, item.roadmapNodePublicId());
        assertEquals(zoneId, item.zoneId());
        assertEquals("먹거리존", item.zoneName());
    }

    /** 이름으로 맞춰 붙이던 시절엔 이름이 같은 두 부스가 한 구역으로 뭉쳤다. 지금은 노드별로 갈린다. */
    @Test
    void 이름이_같은_부스도_구역이_각각_붙는다() {
        UUID leftZone = UUID.randomUUID();
        UUID rightZone = UUID.randomUUID();
        givenBooths(List.of(booth(1L, "커피", 11L), booth(2L, "커피", 12L)));
        givenCongestions(List.of());
        givenZones(List.of(
                new BoothZoneView(11L, UUID.randomUUID(), leftZone, "동문존"),
                new BoothZoneView(12L, UUID.randomUUID(), rightZone, "서문존")
        ));

        List<BoothCongestionResponse> items = userCongestionService
                .getCongestion(FESTIVAL_PUBLIC_ID).booths();

        assertEquals("동문존", items.get(0).zoneName());
        assertEquals("서문존", items.get(1).zoneName());
    }

    /** 지도에 안 찍혔거나 구역으로 안 묶인 부스도 목록에는 그대로 남고 구역만 비어 있다. */
    @Test
    void 구역_미지정_부스는_구역이_비어_있다() {
        UUID nodePublicId = UUID.randomUUID();
        givenBooths(List.of(booth(1L, "지도에 없는 부스", null), booth(2L, "묶이지 않은 부스", 12L)));
        givenCongestions(List.of());
        givenZones(List.of(new BoothZoneView(12L, nodePublicId, null, null)));

        List<BoothCongestionResponse> items = userCongestionService
                .getCongestion(FESTIVAL_PUBLIC_ID).booths();

        assertEquals(2, items.size());
        assertNull(items.get(0).roadmapNodePublicId());
        assertNull(items.get(0).zoneId());
        assertNull(items.get(0).zoneName());
        assertEquals(nodePublicId, items.get(1).roadmapNodePublicId());
        assertNull(items.get(1).zoneId());
        assertNull(items.get(1).zoneName());
    }

    private void givenBooths(List<BoothView> booths) {
        when(festivalQueryService.getFestivalIdByPublicId(FESTIVAL_PUBLIC_ID)).thenReturn(FESTIVAL_ID);
        when(boothQueryService.getBoothsByFestivalId(FESTIVAL_ID)).thenReturn(booths);
    }

    private void givenCongestions(List<BoothCongestionView> congestions) {
        when(congestionQueryService.getLatestByBoothIds(any())).thenReturn(
                congestions.stream().collect(Collectors.toMap(BoothCongestionView::boothId, Function.identity()))
        );
    }

    private void givenZones(List<BoothZoneView> zones) {
        Map<Long, BoothZoneView> byNodeId = zones.stream()
                .collect(Collectors.toMap(BoothZoneView::nodeId, Function.identity()));
        when(roadmapQueryService.getBoothZones(anyLong(), any())).thenReturn(byNodeId);
    }

    private BoothView booth(long boothId, String name, Long roadmapNodeId) {
        return new BoothView(boothId, name, null, null, roadmapNodeId);
    }

    private BoothCongestionView congestion(long boothId, BoothCongestionLevel level, Integer waitMinutes) {
        return new BoothCongestionView(boothId, level, waitMinutes, LocalDateTime.of(2026, 9, 20, 12, 0));
    }
}
