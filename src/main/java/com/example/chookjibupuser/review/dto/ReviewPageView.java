package com.example.chookjibupuser.review.dto;

import java.util.List;

public record ReviewPageView(
        List<ReviewView> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
