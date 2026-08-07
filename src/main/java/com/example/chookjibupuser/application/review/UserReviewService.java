package com.example.chookjibupuser.application.review;

import com.example.chookjibupuser.api.review.dto.ReviewCreateRequest;
import com.example.chookjibupuser.api.review.dto.ReviewPageResponse;
import com.example.chookjibupuser.api.review.dto.ReviewResponse;
import com.example.chookjibupuser.festival.FestivalQueryService;
import com.example.chookjibupuser.review.FestivalReviewService;
import com.example.chookjibupuser.review.dto.ReviewPageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 축제 리뷰(별점+한줄평) 작성/조회 유스케이스를 처리하는 application 계층 조합
 * 서비스이다.
 *
 * <p>review 도메인({@link FestivalReviewService})과 festival 도메인({@link FestivalQueryService})은
 * 서로의 존재를 모른다. QR코드/프론트 URL에 담긴 축제의 public_id(UUID)를 review 도메인이
 * 쓰는 내부 festival_id(Long)로 바꾸는 지점이 바로 여기다 — review 도메인은 UUID의
 * 존재 자체를 모른다.</p>
 */
@Service
@RequiredArgsConstructor
public class UserReviewService {

    private final FestivalQueryService festivalQueryService;
    private final FestivalReviewService festivalReviewService;

    /**
     * @param userId 로그인한 사용자 ID. 이 API는 인증이 필요하므로 null이 오면 안 된다
     *               (컨트롤러가 인증 여부를 먼저 걸러준다).
     */
    public ReviewResponse createReview(UUID festivalPublicId, Long userId, ReviewCreateRequest request) {
        Long festivalId = festivalQueryService.getFestivalIdByPublicId(festivalPublicId);
        var view = festivalReviewService.createReview(userId, festivalId, request.rating(), request.content());
        return ReviewResponse.from(view);
    }

    public ReviewPageResponse getReviews(UUID festivalPublicId, Integer page, Integer size) {
        Long festivalId = festivalQueryService.getFestivalIdByPublicId(festivalPublicId);
        ReviewPageView pageView = festivalReviewService.getReviews(festivalId, page, size);

        return new ReviewPageResponse(
                pageView.items().stream().map(ReviewResponse::from).toList(),
                pageView.page(),
                pageView.size(),
                pageView.totalElements(),
                pageView.totalPages()
        );
    }
}
