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

    /**
     * @param today 서비스 기준 시간대(Asia/Seoul)의 오늘 날짜. 진행 상태 계산의 기준이며,
     *              서버 기본 시간대에 좌우되지 않도록 호출하는 쪽에서 넘겨준다.
     */
    public static FestivalSummaryView of(Festival festival, LocalDate today) {
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
                FestivalProgressStatus.from(today, festival.getStartDate(), festival.getEndDate())
        );
    }
}