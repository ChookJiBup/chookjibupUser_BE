package com.example.chookjibupuser.api.festival.dto;

import java.util.List;

public record UserFestivalPageResponse(
        List<UserFestivalResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
