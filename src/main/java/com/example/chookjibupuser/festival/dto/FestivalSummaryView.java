// festival/dto/FestivalSummaryView.java (전체)
package com.example.chookjibupuser.festival.dto;

import com.example.chookjibupuser.festival.Festival;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record FestivalSummaryView(
        Long festivalId,
        UUID publicId,
        String name,
        String imageUrl,
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
        FestivalProgressStatus progressStatus
) {

    public static FestivalSummaryView of(Festival festival) {
        return new FestivalSummaryView(
                festival.getFestivalId(),
                festival.getPublicId(),
                festival.getFestivalName(),
                festival.getImageUrl(),
                festival.getEventPlace(),
                festival.getRoadAddress(),
                festival.getDetailAddress(),
                festival.getStartDate(),
                festival.getEndDate(),
                festival.getOperationStartTime(),
                festival.getOperationEndTime(),
                festival.getPhoneNumber(),
                festival.getHomepageUrl(),
                festival.getLatitude(),
                festival.getLongitude(),
                FestivalProgressStatus.from(LocalDate.now(), festival.getStartDate(), festival.getEndDate())
        );
    }
}