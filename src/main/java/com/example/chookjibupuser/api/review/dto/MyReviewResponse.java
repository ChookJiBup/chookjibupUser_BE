package com.example.chookjibupuser.api.review.dto;

import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.review.dto.MyReviewEntryView;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * "내가 쓴 리뷰" 화면(마이페이지)의 한 항목 응답이다. UserReviewService가 review 도메인의
 * reviewId + 별점/한줄평, festival 도메인의 축제 요약 정보를 합쳐서 만든다.
 * MyWishlistFestivalResponse와 같은 패턴이다.
 *
 * <p>festivalId(내부 PK)는 응답에 노출하지 않는다 — 프론트가 축제 상세 페이지로
 * 이동할 땐 여기 담긴 {@code festivalPublicId}를 쓴다.</p>
 */
public record MyReviewResponse(
        Long reviewId,
        UUID festivalPublicId,
        String festivalName,
        String festivalImageUrl,
        LocalDate festivalStartDate,
        LocalDate festivalEndDate,
        FestivalProgressStatus festivalProgressStatus,
        int rating,
        String content,
        boolean onsite,
        OffsetDateTime createdAt
) {

    public static MyReviewResponse of(MyReviewEntryView entry, FestivalSummaryView festival) {
        return new MyReviewResponse(
                entry.reviewId(),
                festival.publicId(),
                festival.name(),
                festival.imageUrl(),
                festival.startDate(),
                festival.endDate(),
                festival.progressStatus(),
                entry.rating(),
                entry.content(),
                entry.onsite(),
                entry.createdAt()
        );
    }
}