package com.example.chookjibupuser.application.festival;

import com.example.chookjibupuser.api.festival.dto.BoothCongestionResponse;
import com.example.chookjibupuser.api.festival.dto.BoothResponse;
import com.example.chookjibupuser.api.festival.dto.RoadmapResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalDetailResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalPageResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalResponse;
import com.example.chookjibupuser.booth.BoothQueryService;
import com.example.chookjibupuser.booth.dto.BoothView;
import com.example.chookjibupuser.congestion.CongestionQueryService;
import com.example.chookjibupuser.congestion.dto.BoothCongestionView;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalPageView;
import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.roadmap.RoadmapQueryService;
import com.example.chookjibupuser.wishlist.WishlistQueryService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * "축제 목록/상세 + 로그인한 사용자의 찜 여부 + 로드맵 + 부스 + 혼잡도"를 합쳐서
 * 보여주는 화면 전용 조합 서비스이다.
 *
 * <p>festival, wishlist, roadmap, booth, congestion 다섯 도메인은 서로의 존재를
 * 모른다. 두 도메인 이상을 엮는 지점은 application 계층인 여기 하나로 한정한다 —
 * 도메인 서비스 자체는 순수하게 유지하고, 컨트롤러는 이 서비스 하나만 호출한다.</p>
 */
@Service
@RequiredArgsConstructor
public class UserFestivalService {

    private final FestivalQueryService festivalQueryService;
    private final WishlistQueryService wishlistQueryService;
    private final RoadmapQueryService roadmapQueryService;
    private final BoothQueryService boothQueryService;
    private final CongestionQueryService congestionQueryService;

    /**
     * @param userId 로그인한 사용자 ID. 비회원 조회면 null.
     */
    public UserFestivalPageResponse getFestivals(
            Integer page,
            Integer size,
            Long userId
    ) {
        FestivalPageView pageView = festivalQueryService.searchFestivals(page, size);

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

    /**
     * 축제 상세를 조회한다.
     * - 로드맵/부스 기본 정보는 저장돼 있으면 축제 상태와 무관하게 항상 채운다.
     * - 혼잡도(축제 전체 + 부스별 대기시간)는 축제가 진행중(ONGOING)일 때만 채운다.
     *
     * @param userId 로그인한 사용자 ID. 비회원 조회면 null.
     */
    public UserFestivalDetailResponse getFestivalDetail(Long festivalId, Long userId) {
        FestivalDetailView detail = festivalQueryService.getFestival(festivalId);
        boolean wishlisted = wishlistQueryService.isWishlisted(userId, festivalId);

        RoadmapResponse roadmap = roadmapQueryService.getRoadmap(festivalId)
                .map(RoadmapResponse::from)
                .orElse(null);

        boolean ongoing = detail.progressStatus() == FestivalProgressStatus.ONGOING;
        List<BoothResponse> booths = buildBooths(festivalId, ongoing);
        String festivalCongestionLevel = ongoing
                ? congestionQueryService.getLatestFestivalCongestion(festivalId)
                        .map(c -> c.congestionLevel())
                        .orElse(null)
                : null;

        return UserFestivalDetailResponse.of(detail, wishlisted, festivalCongestionLevel, roadmap, booths);
    }

    private List<BoothResponse> buildBooths(Long festivalId, boolean ongoing) {
        List<BoothView> boothViews = boothQueryService.getBooths(festivalId);

        if (!ongoing || boothViews.isEmpty()) {
            return boothViews.stream().map(b -> BoothResponse.of(b, null)).toList();
        }

        List<Long> boothIds = boothViews.stream().map(BoothView::boothId).toList();
        Map<Long, BoothCongestionView> congestionByBoothId = congestionQueryService.getLatestBoothCongestion(boothIds);

        return boothViews.stream()
                .map(b -> {
                    BoothCongestionView congestion = congestionByBoothId.get(b.boothId());
                    return BoothResponse.of(b, congestion == null ? null : BoothCongestionResponse.from(congestion));
                })
                .toList();
    }
}
