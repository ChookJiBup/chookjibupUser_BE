// api/festival/dto/UserFestivalResponse.java (전체)
package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record UserFestivalResponse(
        UUID publicId,
        String name,
        String eventPlace,
        String address,
        String detailAddress,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime operationStartTime,
        LocalTime operationEndTime,
        String phoneNumber,
        String homepageUrl,
        BigDecimal latitude,
        BigDecimal longitude,
        FestivalProgressStatus progressStatus,
        boolean wishlisted
) {

    public static UserFestivalResponse of(FestivalSummaryView view, boolean wishlisted) {
        return new UserFestivalResponse(
                view.publicId(),
                view.name(),
                view.eventPlace(),
                view.address(),
                view.detailAddress(),
                view.startDate(),
                view.endDate(),
                view.operationStartTime(),
                view.operationEndTime(),
                view.phoneNumber(),
                view.homepageUrl(),
                view.latitude(),
                view.longitude(),
                view.progressStatus(),
                wishlisted
        );
    }
}