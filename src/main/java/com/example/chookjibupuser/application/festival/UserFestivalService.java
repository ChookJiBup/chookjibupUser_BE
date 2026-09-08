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
import org.springframework.stereotype.Service;

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

        RoadmapView roadmapView = roadmapQueryService.getRoadmap(festivalId);
        RoadmapResponse roadmap = roadmapView == null ? null : RoadmapResponse.from(roadmapView);

        return UserFestivalDetailResponse.of(detail, wishlisted, wishlistCount, reviewCount, roadmap);
    }
}