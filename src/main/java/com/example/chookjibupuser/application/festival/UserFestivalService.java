package com.example.chookjibupuser.application.festival;

import com.example.chookjibupuser.api.festival.dto.RoadmapResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalDetailResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalPageResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalResponse;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalPageView;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.review.FestivalReviewService;
import com.example.chookjibupuser.roadmap.RoadmapQueryService;
import com.example.chookjibupuser.roadmap.dto.RoadmapView;
import com.example.chookjibupuser.wishlist.WishlistQueryService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFestivalService {

    private final FestivalQueryService festivalQueryService;
    private final WishlistQueryService wishlistQueryService;
    private final FestivalReviewService festivalReviewService;
    private final RoadmapQueryService roadmapQueryService;

    public UserFestivalPageResponse getFestivals(
            String name,
            String status,
            String region,
            String sort,
            Integer page,
            Integer size,
            Long userId
    ) {
        FestivalPageView pageView = festivalQueryService.searchFestivals(name, status, region, sort, page, size);

        List<Long> festivalIds = pageView.items().stream().map(FestivalSummaryView::festivalId).toList();

        Set<Long> wishlistedIds = (userId == null)
                ? Set.of()
                : wishlistQueryService.getWishlistedFestivalIds(userId, festivalIds);
        Map<Long, Long> wishlistCounts = wishlistQueryService.getWishlistCounts(festivalIds);
        Map<Long, Long> reviewCounts = festivalReviewService.getReviewCounts(festivalIds);

        return new UserFestivalPageResponse(
                pageView.items().stream()
                        .map(item -> UserFestivalResponse.of(
                                item,
                                wishlistedIds.contains(item.festivalId()),
                                wishlistCounts.getOrDefault(item.festivalId(), 0L),
                                reviewCounts.getOrDefault(item.festivalId(), 0L)
                        ))
                        .toList(),
                pageView.page(),
                pageView.size(),
                pageView.totalElements(),
                pageView.totalPages()
        );
    }

    public UserFestivalDetailResponse getFestivalDetail(UUID festivalPublicId, Long userId) {
        Long festivalId = festivalQueryService.getFestivalIdByPublicId(festivalPublicId);
        FestivalDetailView detail = festivalQueryService.getFestival(festivalId);
        boolean wishlisted = wishlistQueryService.isWishlisted(userId, festivalId);

        List<Long> singleId = List.of(festivalId);
        long wishlistCount = wishlistQueryService.getWishlistCounts(singleId).getOrDefault(festivalId, 0L);
        long reviewCount = festivalReviewService.getReviewCounts(singleId).getOrDefault(festivalId, 0L);

        return UserFestivalDetailResponse.of(
                detail,
                wishlisted,
                wishlistCount,
                reviewCount,
                readRoadmapOrNull(festivalId, festivalPublicId)
        );
    }

    /**
     * 부스지도를 읽는다. 실패하면 배치도만 포기하고 나머지 축제 정보는 그대로 내려준다.
     *
     * <p>배치도 한 장이 깨졌다고 축제 상세가 통째로 500이 나면 방문객은 축제 이름조차 못 본다.
     * 실제로 지도를 한 번 교체한 축제 3개에서 상세 API가 전부 죽어 있었다. 배치도는 축제 상세의
     * 부가 정보이므로, 여기서 삼키고 「아직 배치도가 공개되지 않았어요」로 보이게 둔다.</p>
     *
     * <p>다만 조용히 삼키면 같은 문제가 또 묻힌다. 어느 축제에서 무엇 때문에 실패했는지
     * 경고 로그로 남긴다.</p>
     */
    private RoadmapResponse readRoadmapOrNull(Long festivalId, UUID festivalPublicId) {
        try {
            RoadmapView roadmapView = roadmapQueryService.getRoadmap(festivalId);
            return roadmapView == null ? null : RoadmapResponse.from(roadmapView);
        } catch (RuntimeException exception) {
            log.warn(
                    "부스지도를 읽지 못해 축제 상세에서 제외합니다. festivalId={}, festivalPublicId={}",
                    festivalId,
                    festivalPublicId,
                    exception
            );
            return null;
        }
    }
}