package com.example.chookjibupuser.application.festival;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.chookjibupuser.api.festival.dto.UserFestivalDetailResponse;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import com.example.chookjibupuser.review.FestivalReviewService;
import com.example.chookjibupuser.roadmap.RoadmapQueryService;
import com.example.chookjibupuser.wishlist.WishlistQueryService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * 축제 상세는 배치도가 없어도, 배치도를 읽다가 터져도 나머지 정보를 그대로 내려줘야 한다.
 *
 * <p>지도를 한 번 교체한 축제 3개에서 부스지도 조회가 예외를 던져 축제 상세 API 전체가
 * 500이었다. 방문객은 축제 이름조차 못 봤다. 배치도는 축제 상세의 부가 정보일 뿐이다.</p>
 */
class UserFestivalServiceTest {

    private static final long FESTIVAL_ID = 409L;
    private static final UUID FESTIVAL_PUBLIC_ID = UUID.fromString("f23a3d5b-1af9-4e89-9efe-fdb0b1586ab5");

    private final FestivalQueryService festivalQueryService = mock(FestivalQueryService.class);
    private final WishlistQueryService wishlistQueryService = mock(WishlistQueryService.class);
    private final FestivalReviewService festivalReviewService = mock(FestivalReviewService.class);
    private final RoadmapQueryService roadmapQueryService = mock(RoadmapQueryService.class);

    private final UserFestivalService userFestivalService = new UserFestivalService(
            festivalQueryService,
            wishlistQueryService,
            festivalReviewService,
            roadmapQueryService
    );

    @Test
    void 부스지도_조회가_실패해도_축제_상세는_내려간다() {
        givenFestival();
        when(roadmapQueryService.getRoadmap(FESTIVAL_ID))
                .thenThrow(new IncorrectResultSizeDataAccessException(1, 2));

        UserFestivalDetailResponse response =
                userFestivalService.getFestivalDetail(FESTIVAL_PUBLIC_ID, null);

        assertEquals("2014 안양 아줌마축제", response.name());
        // 배치도만 빠지고 화면은 「아직 배치도가 공개되지 않았어요」로 보인다.
        assertNull(response.roadmap());
    }

    @Test
    void 부스지도가_공개되지_않았으면_roadmap은_null이다() {
        givenFestival();
        when(roadmapQueryService.getRoadmap(FESTIVAL_ID)).thenReturn(null);

        UserFestivalDetailResponse response =
                userFestivalService.getFestivalDetail(FESTIVAL_PUBLIC_ID, null);

        assertEquals("2014 안양 아줌마축제", response.name());
        assertNull(response.roadmap());
    }

    private void givenFestival() {
        when(festivalQueryService.getFestivalIdByPublicId(FESTIVAL_PUBLIC_ID)).thenReturn(FESTIVAL_ID);
        when(festivalQueryService.getFestival(FESTIVAL_ID)).thenReturn(detailView());
        when(wishlistQueryService.isWishlisted(null, FESTIVAL_ID)).thenReturn(false);
        when(wishlistQueryService.getWishlistCounts(List.of(FESTIVAL_ID))).thenReturn(Map.of());
        when(festivalReviewService.getReviewCounts(List.of(FESTIVAL_ID))).thenReturn(Map.of());
    }

    private FestivalDetailView detailView() {
        return new FestivalDetailView(
                FESTIVAL_ID,
                FESTIVAL_PUBLIC_ID,
                "2014 안양 아줌마축제",
                null,
                null,
                "경기도 안양시",
                null,
                LocalDate.of(2014, 5, 1),
                LocalDate.of(2014, 5, 3),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                FestivalProgressStatus.COMPLETED
        );
    }
}
