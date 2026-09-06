package com.example.chookjibupuser.festival.dto;

import java.time.LocalDate;

public enum FestivalProgressStatus {
    UPCOMING,
    ONGOING,
    COMPLETED;

    public static FestivalProgressStatus from(LocalDate today, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        if (today.isBefore(startDate)) {
            return UPCOMING;
        }
        if (today.isAfter(endDate)) {
            return COMPLETED;
        }
        return ONGOING;
    }
}