package com.example.chookjibupuser.application.wishlist;

import com.example.chookjibupuser.api.wishlist.dto.MyWishlistFestivalResponse;
import com.example.chookjibupuser.api.wishlist.dto.MyWishlistPageResponse;
import com.example.chookjibupuser.api.wishlist.dto.WishlistToggleResponse;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.review.FestivalReviewService;
import com.example.chookjibupuser.wishlist.WishlistCommandService;
import com.example.chookjibupuser.wishlist.WishlistQueryService;
import com.example.chookjibupuser.wishlist.dto.WishlistEntryPageView;
import com.example.chookjibupuser.wishlist.dto.WishlistEntryView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 축제 찜 유스케이스(토글/내 찜 목록)를 처리하는 application 계층 조합 서비스이다.
 *
 * <p>wishlist 도메인({@link WishlistCommandService}, {@link WishlistQueryService}),
 * festival 도메인({@link FestivalQueryService}), review 도메인({@link FestivalReviewService})은
 * 서로의 존재를 모른다. 두 도메인 이상을 엮는 지점(찜 토글 시 축제 존재 확인, 내 찜 목록에
 * 축제 상세·찜/리뷰 개수 합치기)은 application 계층인 여기 하나로 한정한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserWishlistService {

    private final WishlistCommandService wishlistCommandService;
    private final WishlistQueryService wishlistQueryService;
    private final FestivalQueryService festivalQueryService;
    private final FestivalReviewService festivalReviewService;

    /**
     * 하트 클릭 = 찜 토글. 이미 찜했으면 취소하고, 안 했으면 찜한다.
     * 어느 쪽이든 실패로 취급하지 않는다 — 존재하지 않는 축제일 때만 예외를 던진다.
     */
    public WishlistToggleResponse toggle(Long userId, UUID festivalPublicId) {
        Long festivalId = festivalQueryService.getFestivalIdByPublicId(festivalPublicId);
        return new WishlistToggleResponse(festivalPublicId, toggleSafely(userId, festivalId));
    }

    private boolean toggleSafely(Long userId, Long festivalId) {
        try {
            return wishlistCommandService.toggle(userId, festivalId);
        } catch (DataIntegrityViolationException exception) {
            log.info(
                    "찜 토글 중 동시 요청 충돌 감지 - 이미 찜된 것으로 처리합니다. userId={}, festivalId={}",
                    userId,
                    festivalId
            );
            return true;
        }
    }

    /**
     * 찜 목록 편집 모드(체크박스 다중 선택)의 "삭제하기"에 매핑된다. festivalPublicId를
     * 내부 festivalId로 바꾼 뒤, wishlist 도메인에 실제 삭제를 위임한다.
     */
    public void deleteAll(Long userId, List<UUID> festivalPublicIds) {
        List<Long> festivalIds = festivalPublicIds.stream()
                .map(festivalQueryService::getFestivalIdByPublicId)
                .toList();
        wishlistCommandService.deleteAll(userId, festivalIds);
    }

    /**
     * 내가 찜한 축제 목록을 최신순으로 조회한다. 항목마다 찜/리뷰 개수도 같이 채운다.
     */
    public MyWishlistPageResponse getMyWishlist(Long userId, Integer page, Integer size) {
        WishlistEntryPageView entryPage = wishlistQueryService.getMyWishlist(userId, page, size);

        List<Long> festivalIds = entryPage.items().stream().map(WishlistEntryView::festivalId).toList();
        Map<Long, FestivalSummaryView> festivalById = festivalQueryService.getFestivalsByIds(festivalIds);
        Map<Long, Long> wishlistCounts = wishlistQueryService.getWishlistCounts(festivalIds);
        Map<Long, Long> reviewCounts = festivalReviewService.getReviewCounts(festivalIds);

        List<MyWishlistFestivalResponse> items = entryPage.items().stream()
                .map(entry -> toResponse(entry, festivalById, wishlistCounts, reviewCounts))
                .filter(response -> response != null)
                .toList();

        return new MyWishlistPageResponse(
                items,
                entryPage.page(),
                entryPage.size(),
                entryPage.totalElements(),
                entryPage.totalPages()
        );
    }

    private MyWishlistFestivalResponse toResponse(
            WishlistEntryView entry,
            Map<Long, FestivalSummaryView> festivalById,
            Map<Long, Long> wishlistCounts,
            Map<Long, Long> reviewCounts
    ) {
        FestivalSummaryView festival = festivalById.get(entry.festivalId());
        if (festival == null) {
            // 찜한 뒤에 축제 데이터가 파이프라인에서 지워진 것 같은 드문 경우 — 화면에서 조용히 제외한다.
            return null;
        }
        return MyWishlistFestivalResponse.of(
                festival,
                wishlistCounts.getOrDefault(entry.festivalId(), 0L),
                reviewCounts.getOrDefault(entry.festivalId(), 0L),
                entry.wishlistedAt()
        );
    }
}