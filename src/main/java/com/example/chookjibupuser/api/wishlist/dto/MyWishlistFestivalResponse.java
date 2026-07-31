package com.example.chookjibupuser.api.wishlist.dto;

import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * "내 찜 목록" 화면의 한 항목 응답이다. UserWishlistService가 wishlist 도메인의
 * festivalId + wishlistedAt과 festival 도메인의 상세 정보를 합쳐서 만든다.
 */
public record MyWishlistFestivalResponse(
        Long festivalId,
        String name,
        String eventPlace,
        String address,
        LocalDate startDate,
        LocalDate endDate,
        OffsetDateTime wishlistedAt
) {

    public static MyWishlistFestivalResponse of(FestivalSummaryView festival, OffsetDateTime wishlistedAt) {
        return new MyWishlistFestivalResponse(
                festival.festivalId(),
                festival.name(),
                festival.eventPlace(),
                festival.address(),
                festival.startDate(),
                festival.endDate(),
                wishlistedAt
        );
    }
}
