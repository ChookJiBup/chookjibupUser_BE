// api/festival/dto/UserFestivalDetailResponse.java (전체)
package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** roadmap은 관리자가 PUBLISHED 해둔 경우에만 채워지고, 아니면 null. */
public record UserFestivalDetailResponse(
        UUID publicId, String name, String eventPlace, String address, String detailAddress,
        LocalDate startDate, LocalDate endDate, LocalTime operationStartTime, LocalTime operationEndTime,
        String content, String phoneNumber, String homepageUrl,
        BigDecimal latitude, BigDecimal longitude,
        FestivalProgressStatus progressStatus,
        boolean wishlisted, RoadmapResponse roadmap
) {
    public static UserFestivalDetailResponse of(FestivalDetailView view, boolean wishlisted, RoadmapResponse roadmap) {
        return new UserFestivalDetailResponse(view.publicId(), view.name(), view.eventPlace(), view.address(),
                view.detailAddress(), view.startDate(), view.endDate(), view.operationStartTime(),
                view.operationEndTime(), view.content(), view.phoneNumber(), view.homepageUrl(),
                view.latitude(), view.longitude(),
                view.progressStatus(), wishlisted, roadmap);
    }
}