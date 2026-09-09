package com.example.chookjibupuser.festival;

import com.example.chookjibupuser.api.festival.dto.UserFestivalResponse;
import com.example.chookjibupuser.api.festival.dto.UserFestivalDetailResponse;
import com.example.chookjibupuser.api.wishlist.dto.MyWishlistFestivalResponse;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FestivalImageResponseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 9);

    @Test
    void preservesImageAcrossListDetailAndWishlistResponses() {
        Festival festival = mock(Festival.class);
        String imageUrl = "https://example.com/festival-poster.jpg";
        when(festival.getImageUrl()).thenReturn(imageUrl);
        var summary = FestivalSummaryView.of(festival, TODAY);
        var detail = FestivalDetailView.of(festival, TODAY);

        assertEquals(imageUrl, UserFestivalResponse.of(summary, false, 0, 0).imageUrl());
        assertEquals(imageUrl, UserFestivalDetailResponse.of(detail, false, 0, 0, null).imageUrl());
        assertEquals(imageUrl, MyWishlistFestivalResponse.of(summary, 0, 0, null).imageUrl());
    }

    @Test
    void supportsExistingFestivalsWithoutAnImage() {
        Festival festival = mock(Festival.class);
        var summary = FestivalSummaryView.of(festival, TODAY);
        assertNull(UserFestivalResponse.of(summary, false, 0, 0).imageUrl());
        assertNull(UserFestivalDetailResponse.of(FestivalDetailView.of(festival, TODAY), false, 0, 0, null).imageUrl());
        assertNull(MyWishlistFestivalResponse.of(summary, 0, 0, null).imageUrl());
    }
}
