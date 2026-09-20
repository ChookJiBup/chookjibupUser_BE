// festival/dto/FestivalDetailView.java (전체)
package com.example.chookjibupuser.festival.dto;

import com.example.chookjibupuser.festival.Festival;
import com.example.chookjibupuser.festival.FestivalCoordinate;
import com.example.chookjibupuser.festival.FestivalCoordinateResolver;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record FestivalDetailView(
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
        String content,
        String phoneNumber,
        String homepageUrl,
        BigDecimal latitude,
        BigDecimal longitude,
        FestivalProgressStatus progressStatus,
        Long viewCount
) {

    /**
     * @param today 서비스 기준 시간대(Asia/Seoul)의 오늘 날짜. 진행 상태 계산의 기준이며,
     *              서버 기본 시간대에 좌우되지 않도록 호출하는 쪽에서 넘겨준다.
     */
    public static FestivalDetailView of(Festival festival, LocalDate today) {
        return of(
                festival,
                today,
                new FestivalCoordinate(festival.getLatitude(), festival.getLongitude())
        );
    }

    /**
     * 좌표를 바깥에서 정해 넘기는 형태.
     *
     * 관리자 콘솔이 등록한 축제는 좌표가 `festivals`가 아니라 `festival_locations`에 있어,
     * 엔티티만 보면 «좌표 없음»이 된다. 어디서 온 좌표인지는 {@link FestivalCoordinateResolver}가 정한다.
     */
    public static FestivalDetailView of(Festival festival, LocalDate today, FestivalCoordinate coordinate) {
        return new FestivalDetailView(
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
                festival.getContent(),
                festival.getPhoneNumber(),
                festival.getHomepageUrl(),
                coordinate.latitude(),
                coordinate.longitude(),
                FestivalProgressStatus.from(today, festival.getStartDate(), festival.getEndDate()),
                festival.getViewCount()
        );
    }
}