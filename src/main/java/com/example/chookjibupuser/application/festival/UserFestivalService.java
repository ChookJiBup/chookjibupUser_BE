// application/festival/UserFestivalService.java (전체)
package com.example.chookjibupuser.application.festival;

import com.example.chookjibupuser.api.festival.dto.RoadmapResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalDetailResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalPageResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalResponse;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalPageView;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.roadmap.RoadmapQueryService;
import com.example.chookjibupuser.roadmap.dto.RoadmapView;
import com.example.chookjibupuser.wishlist.WishlistQueryService;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFestivalService {

    private final FestivalQueryService festivalQueryService;
    private final WishlistQueryService wishlistQueryService;
    private final RoadmapQueryService roadmapQueryService;

    public UserFestivalPageResponse getFestivals(
            String name,
            String status,
            String sort,
            Integer page,
            Integer size,
            Long userId
    ) {
        FestivalPageView pageView = festivalQueryService.searchFestivals(name, status, sort, page, size);

        Set<Long> wishlistedIds = (userId == null)
                ? Set.of()
                : wishlistQueryService.getWishlistedFestivalIds(
                userId,
                pageView.items().stream().map(FestivalSummaryView::festivalId).toList()
        );

        return new UserFestivalPageResponse(
                pageView.items().stream()
                        .map(item -> UserFestivalResponse.of(item, wishlistedIds.contains(item.festivalId())))
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

        RoadmapView roadmapView = roadmapQueryService.getRoadmap(festivalId);
        RoadmapResponse roadmap = roadmapView == null ? null : RoadmapResponse.from(roadmapView);

        return UserFestivalDetailResponse.of(detail, wishlisted, roadmap);
    }
}