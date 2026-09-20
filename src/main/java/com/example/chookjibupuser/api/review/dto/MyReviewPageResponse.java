package com.example.chookjibupuser.api.review.dto;

import java.util.List;

public record MyReviewPageResponse(
        List<MyReviewResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}