package com.example.chookjibupuser.roadmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.chookjibupuser.roadmap.dto.RoadmapView;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * 부스지도는 관리자가 「방문객에게 공개」를 누른 로드맵(PUBLISHED)만 내려간다.
 * 검토 중인 배치가 방문객 화면에 먼저 나가지 않게 하는 게이트다.
 */
class RoadmapQueryServiceTest {

    private final FestivalRoadmapRepository festivalRoadmapRepository = mock(FestivalRoadmapRepository.class);
    private final RoadmapNodeRepository roadmapNodeRepository = mock(RoadmapNodeRepository.class);
    private final FestivalMapRepository festivalMapRepository = mock(FestivalMapRepository.class);
    private final FestivalMapPresentationRepository presentationRepository =
            mock(FestivalMapPresentationRepository.class);

    private final RoadmapQueryService roadmapQueryService = new RoadmapQueryService(
            festivalRoadmapRepository,
            roadmapNodeRepository,
            festivalMapRepository,
            presentationRepository,
            new MapImageProperties(null)
    );

    private static final long FESTIVAL_ID = 7L;
    private static final long CURRENT_MAP_ID = 75L;

    @Test
    void 로드맵이_없으면_null을_내려준다() {
        when(festivalRoadmapRepository.findByFestivalId(FESTIVAL_ID)).thenReturn(Optional.empty());

        assertNull(roadmapQueryService.getRoadmap(FESTIVAL_ID));
    }

    @ParameterizedTest
    @EnumSource(value = RoadmapStatus.class, names = "PUBLISHED", mode = EnumSource.Mode.EXCLUDE)
    void 공개되지_않은_로드맵은_부스가_확인됐어도_내려주지_않는다(RoadmapStatus status) {
        FestivalRoadmap roadmap = roadmap(status);
        List<RoadmapNode> nodes = List.of(confirmedBooth());
        when(festivalRoadmapRepository.findByFestivalId(FESTIVAL_ID)).thenReturn(Optional.of(roadmap));
        when(roadmapNodeRepository.findByRoadmapIdOrderBySortOrderAsc(roadmap.getId())).thenReturn(nodes);

        assertNull(roadmapQueryService.getRoadmap(FESTIVAL_ID));
    }

    @Test
    void 공개된_로드맵은_확인된_부스를_내려준다() {
        FestivalRoadmap roadmap = roadmap(RoadmapStatus.PUBLISHED);
        List<RoadmapNode> nodes = List.of(confirmedBooth());
        when(festivalRoadmapRepository.findByFestivalId(FESTIVAL_ID)).thenReturn(Optional.of(roadmap));
        when(roadmapNodeRepository.findByRoadmapIdOrderBySortOrderAsc(roadmap.getId())).thenReturn(nodes);
        when(festivalMapRepository.findById(CURRENT_MAP_ID)).thenReturn(Optional.empty());

        RoadmapView view = roadmapQueryService.getRoadmap(FESTIVAL_ID);

        assertNotNull(view);
        // 구역에 묶이지 않은 부스는 «구역 미지정»으로 모아 내려간다.
        assertEquals(1, view.zones().size());
        assertEquals(1, view.zones().get(0).booths().size());
    }

    /**
     * 지도를 한 번이라도 교체하면 {@code festival_maps}에 예전 판이 남아 축제당 행이 여러 개가 된다.
     * 축제로 지도를 찾으면 그런 축제에서 결과 개수 예외가 나므로, 로드맵이 가리키는 지도만 집는다.
     */
    @Test
    void 지도를_교체한_축제도_로드맵이_가리키는_지도만_본다() {
        FestivalRoadmap roadmap = roadmap(RoadmapStatus.PUBLISHED);
        FestivalMap currentMap = mock(FestivalMap.class);
        when(currentMap.getId()).thenReturn(CURRENT_MAP_ID);
        when(currentMap.getDisplayImageKey()).thenReturn("maps/current.png");
        when(festivalRoadmapRepository.findByFestivalId(FESTIVAL_ID)).thenReturn(Optional.of(roadmap));
        when(roadmapNodeRepository.findByRoadmapIdOrderBySortOrderAsc(roadmap.getId())).thenReturn(List.of());
        when(festivalMapRepository.findById(CURRENT_MAP_ID)).thenReturn(Optional.of(currentMap));
        when(presentationRepository.findByMapId(CURRENT_MAP_ID)).thenReturn(Optional.empty());

        RoadmapView view = mapImageAwareService().getRoadmap(FESTIVAL_ID);

        assertNotNull(view);
        assertEquals("https://cdn.example.com/maps/current.png", view.mapImageUrl());
    }

    /** 지도 이미지 URL까지 확인하려면 image-base-url이 채워진 서비스가 필요하다. */
    private RoadmapQueryService mapImageAwareService() {
        return new RoadmapQueryService(
                festivalRoadmapRepository,
                roadmapNodeRepository,
                festivalMapRepository,
                presentationRepository,
                new MapImageProperties("https://cdn.example.com/")
        );
    }

    private FestivalRoadmap roadmap(RoadmapStatus status) {
        FestivalRoadmap roadmap = mock(FestivalRoadmap.class);
        when(roadmap.getId()).thenReturn(1L);
        when(roadmap.getPublicId()).thenReturn(UUID.randomUUID());
        when(roadmap.getStatus()).thenReturn(status);
        when(roadmap.getZones()).thenReturn(List.of());
        when(roadmap.getCurrentMapId()).thenReturn(CURRENT_MAP_ID);
        return roadmap;
    }

    private RoadmapNode confirmedBooth() {
        RoadmapNode node = mock(RoadmapNode.class);
        when(node.getPublicId()).thenReturn(UUID.randomUUID());
        when(node.isBooth()).thenReturn(true);
        when(node.isConfirmed()).thenReturn(true);
        return node;
    }
}
