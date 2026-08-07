package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import com.example.chookjibupuser.festival.dto.FestivalSummaryView;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 사용자에게 노출하는 축제 요약 응답이다. wishlisted는 festival 도메인이 아니라
 * api 계층(UserFestivalService)이 wishlist 도메인 조회 결과로 채워 넣는다.
 *
 * <p>{@code publicId}가 프론트가 상세/찜/리뷰 API를 호출할 때 쓰는 값이다.
 * festivalId(내부 PK)는 응답에 아예 노출하지 않는다.</p>
 */
public record UserFestivalResponse(
        UUID publicId,
        String name,
        String eventPlace,
        String address,
        LocalDate startDate,
        LocalDate endDate,
        String phoneNumber,
        String homepageUrl,
        FestivalProgressStatus progressStatus,
        boolean wishlisted
) {

    public static UserFestivalResponse of(FestivalSummaryView view, boolean wishlisted) {
        return new UserFestivalResponse(
                view.publicId(),
                view.name(),
                view.eventPlace(),
                view.address(),
                view.startDate(),
                view.endDate(),
                view.phoneNumber(),
                view.homepageUrl(),
                view.progressStatus(),
                wishlisted
        );
    }
}
