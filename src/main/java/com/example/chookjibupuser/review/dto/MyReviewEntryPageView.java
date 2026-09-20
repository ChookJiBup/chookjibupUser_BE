package com.example.chookjibupuser.review.dto;

import java.util.List;

public record MyReviewEntryPageView(
        List<MyReviewEntryView> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}