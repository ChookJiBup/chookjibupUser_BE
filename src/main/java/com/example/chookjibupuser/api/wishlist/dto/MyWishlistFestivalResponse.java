// api/wishlist/dto/MyWishlistFestivalResponse.java (전체)
package com.example.chookjibupuser.api.wishlist.dto;

import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MyWishlistFestivalResponse(
        UUID festivalPublicId,
        String name,
        String eventPlace,
        String address,
        LocalDate startDate,
        LocalDate endDate,
        FestivalProgressStatus progressStatus,
        OffsetDateTime wishlistedAt
) {

    public static MyWishlistFestivalResponse of(FestivalSummaryView festival, OffsetDateTime wishlistedAt) {
        return new MyWishlistFestivalResponse(
                festival.publicId(),
                festival.name(),
                festival.eventPlace(),
                festival.address(),
                festival.startDate(),
                festival.endDate(),
                festival.progressStatus(),
                wishlistedAt
        );
    }
}