package com.example.chookjibupuser.api.wishlist.dto;

import java.util.List;

public record MyWishlistPageResponse(
        List<MyWishlistFestivalResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
