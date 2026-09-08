package com.example.chookjibupuser.api.wishlist.dto;

import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * "내 찜 목록" 화면의 한 항목 응답이다. UserWishlistService가 wishlist 도메인의
 * festivalId + wishlistedAt, festival 도메인의 상세 정보, wishlist/review 도메인의
 * 개수(wishlistCount/reviewCount)를 합쳐서 만든다.
 *
 * <p>festivalId(내부 PK)는 응답에 노출하지 않는다 — 프론트가 상세/찜/리뷰 API를
 * 호출할 땐 여기 담긴 {@code festivalPublicId}를 쓴다.</p>
 */
public record MyWishlistFestivalResponse(
        UUID festivalPublicId,
        String name,
        String imageUrl,
        String eventPlace,
        String address,
        LocalDate startDate,
        LocalDate endDate,
        FestivalProgressStatus progressStatus,
        long wishlistCount,
        long reviewCount,
        OffsetDateTime wishlistedAt
) {

    public static MyWishlistFestivalResponse of(
            FestivalSummaryView festival,
            long wishlistCount,
            long reviewCount,
            OffsetDateTime wishlistedAt
    ) {
        return new MyWishlistFestivalResponse(
                festival.publicId(),
                festival.name(),
                festival.imageUrl(),
                festival.eventPlace(),
                festival.address(),
                festival.startDate(),
                festival.endDate(),
                festival.progressStatus(),
                wishlistCount,
                reviewCount,
                wishlistedAt
        );
    }
}