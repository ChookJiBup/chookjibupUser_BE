package com.example.chookjibupuser.application.review;

import com.example.chookjibupuser.api.review.dto.ReviewCreateRequest;
import com.example.chookjibupuser.api.review.dto.ReviewPageResponse;
import com.example.chookjibupuser.api.review.dto.ReviewResponse;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.review.FestivalReviewService;
import com.example.chookjibupuser.review.dto.ReviewPageView;
import com.example.chookjibupuser.review.dto.ReviewView;
import com.example.chookjibupuser.user.UserQueryService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 축제 리뷰(별점+한줄평) 작성/조회 유스케이스를 처리하는 application 계층 조합
 * 서비스이다.
 *
 * <p>review 도메인({@link FestivalReviewService}), festival 도메인({@link FestivalQueryService}),
 * user 도메인({@link UserQueryService})은 서로의 존재를 모른다. QR코드/프론트 URL에 담긴
 * 축제의 public_id(UUID)를 review 도메인이 쓰는 내부 festival_id(Long)로 바꾸고, 작성자
 * userId를 표시용 닉네임으로 바꾸는 지점이 바로 여기다.</p>
 */
@Service
@RequiredArgsConstructor
public class UserReviewService {

    private final FestivalQueryService festivalQueryService;
    private final FestivalReviewService festivalReviewService;
    private final UserQueryService userQueryService;

    /**
     * @param userId 로그인한 사용자 ID. 비로그인이면 null — 이 경우 request.onsite()가
     *               true여야만(현장 QR 리뷰) 통과한다. false인데 userId가 null이면
     *               "로그인이 필요합니다" 에러를 던진다(일반 리뷰는 로그인 필수).
     */
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
}