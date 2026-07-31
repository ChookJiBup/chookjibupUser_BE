package com.example.chookjibupuser.festival.dto;

import com.example.chookjibupuser.festival.FestivalRow;
import java.time.LocalDate;

/**
 * 축제 상세 화면 전용 뷰이다. 목록용 {@link FestivalSummaryView}와 달리
 * content(축제 내용)까지 포함한다.
 */
public record FestivalDetailView(
        Long festivalId,
        String name,
        String eventPlace,
        String address,
        LocalDate startDate,
        LocalDate endDate,
        String content,
        String phoneNumber,
        String homepageUrl,
        FestivalProgressStatus progressStatus
) {

    public static FestivalDetailView of(FestivalRow row) {
        return new FestivalDetailView(
                row.getFestivalId(),
                row.getFestivalName(),
                row.getEventPlace(),
                row.getRoadAddress(),
                row.getStartDate(),
                row.getEndDate(),
                row.getContent(),
                row.getPhoneNumber(),
                row.getHomepageUrl(),
                FestivalProgressStatus.fromDbValue(row.getProgressStatus())
        );
    }
}
