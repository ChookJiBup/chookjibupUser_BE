package com.example.chookjibupuser.application.review;

import com.example.chookjibupuser.api.review.dto.*;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.review.FestivalReviewService;
import com.example.chookjibupuser.review.dto.MyReviewEntryPageView;
import com.example.chookjibupuser.review.dto.MyReviewEntryView;
import com.example.chookjibupuser.review.dto.ReviewPageView;
import com.example.chookjibupuser.review.dto.ReviewView;
import com.example.chookjibupuser.user.UserQueryService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReviewService {

    private final FestivalQueryService festivalQueryService;
    private final FestivalReviewService festivalReviewService;
    private final UserQueryService userQueryService;

    public ReviewResponse createReview(UUID festivalPublicId, Long userId, ReviewCreateRequest request) {
        if (userId == null && !Boolean.TRUE.equals(request.onsite())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long festivalId = festivalQueryService.getFestivalIdByPublicId(festivalPublicId);
        ReviewView view = festivalReviewService.createReview(
                userId, festivalId, request.rating(), request.content(), Boolean.TRUE.equals(request.onsite())
        );

        String reviewerName = userId == null
                ? null
                : userQueryService.getNicknames(List.of(userId)).get(userId);
        return ReviewResponse.from(view, reviewerName);
    }

    public ReviewPageResponse getReviews(UUID festivalPublicId, Integer page, Integer size) {
        Long festivalId = festivalQueryService.getFestivalIdByPublicId(festivalPublicId);
        ReviewPageView pageView = festivalReviewService.getReviews(festivalId, page, size);

        List<Long> reviewerIds = pageView.items().stream()
                .map(ReviewView::userId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> nicknamesByUserId = userQueryService.getNicknames(reviewerIds);

        return new ReviewPageResponse(
                pageView.items().stream()
                        .map(view -> ReviewResponse.from(view, nicknamesByUserId.get(view.userId())))
                        .toList(),
                pageView.page(),
                pageView.size(),
                pageView.totalElements(),
                pageView.totalPages()
        );
    }

    /**
     * 마이페이지 "내가 쓴 리뷰" 목록. 내가 쓴 리뷰(review 도메인)에 각 축제의 요약 정보
     * (festival 도메인)를 합쳐서 돌려준다 — MyWishlistFestivalResponse를 만드는
     * getMyWishlist와 같은 패턴이다.
     */
    public MyReviewPageResponse getMyReviews(Long userId, Integer page, Integer size) {
        MyReviewEntryPageView entryPage = festivalReviewService.getMyReviews(userId, page, size);

        List<Long> festivalIds = entryPage.items().stream()
                .map(MyReviewEntryView::festivalId)
                .distinct()
                .toList();
        Map<Long, FestivalSummaryView> festivalById = festivalQueryService.getFestivalsByIds(festivalIds);

        List<MyReviewResponse> items = entryPage.items().stream()
                .map(entry -> toMyReviewResponse(entry, festivalById))
                .filter(response -> response != null)
                .toList();

        return new MyReviewPageResponse(
                items,
                entryPage.page(),
                entryPage.size(),
                entryPage.totalElements(),
                entryPage.totalPages()
        );
    }

    private MyReviewResponse toMyReviewResponse(
            MyReviewEntryView entry,
            Map<Long, FestivalSummaryView> festivalById
    ) {
        FestivalSummaryView festival = festivalById.get(entry.festivalId());
        if (festival == null) {
            // 리뷰를 쓴 뒤 축제 데이터가 파이프라인에서 지워진 것 같은 드문 경우 — 화면에서 조용히 제외한다.
            return null;
        }
        return MyReviewResponse.of(entry, festival);
    }

    public ReviewResponse updateReview(Long userId, Long reviewId, ReviewUpdateRequest request) {
        ReviewView view = festivalReviewService.updateReview(
                userId, reviewId, request.rating(), request.content()
        );
        String reviewerName = userQueryService.getNicknames(List.of(userId)).get(userId);
        return ReviewResponse.from(view, reviewerName);
    }


    public void deleteReview(Long userId, Long reviewId) {
        festivalReviewService.deleteReview(userId, reviewId);
    }
}