package com.example.chookjibupuser.wishlist.dto;

import java.util.List;

public record WishlistEntryPageView(
        List<WishlistEntryView> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
