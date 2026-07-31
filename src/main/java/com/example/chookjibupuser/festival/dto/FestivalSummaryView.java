package com.example.chookjibupuser.festival.dto;

import com.example.chookjibupuser.festival.FestivalRow;
import java.time.LocalDate;

/**
 * festival 도메인의 순수한 뷰이다. 찜 여부 같은 다른 도메인 정보는 담지 않는다
 * (그건 api 계층에서 조합할 때 별도로 얹는다).
 */
public record FestivalSummaryView(
        Long festivalId,
        String name,
        String eventPlace,
        String address,
        LocalDate startDate,
        LocalDate endDate,
        String phoneNumber,
        String homepageUrl,
        FestivalProgressStatus progressStatus
) {

    public static FestivalSummaryView of(FestivalRow row) {
        return new FestivalSummaryView(
                row.getFestivalId(),
                row.getFestivalName(),
                row.getEventPlace(),
                row.getRoadAddress(),
                row.getStartDate(),
                row.getEndDate(),
                row.getPhoneNumber(),
                row.getHomepageUrl(),
                FestivalProgressStatus.fromDbValue(row.getProgressStatus())
        );
    }
}
