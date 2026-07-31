package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.festival.dto.FestivalDetailView;
import com.example.chookjibupuser.festival.dto.FestivalProgressStatus;
import java.time.LocalDate;
import java.util.List;

/**
 * 축제 상세 화면 응답이다. festival 도메인 자체 정보 외에, 다른 도메인 조회 결과를
 * api 계층(UserFestivalService)이 조합해서 채운다.
 *
 * <ul>
 *   <li>roadmap, booths — 저장돼 있으면 축제 상태와 무관하게 항상 채움 (없으면 roadmap=null, booths=[])</li>
 *   <li>festivalCongestionLevel, 각 부스의 congestion — 축제가 진행중(ONGOING)일 때만 채움</li>
 * </ul>
 */
public record UserFestivalDetailResponse(
        Long festivalId,
        String name,
        String eventPlace,
        String address,
        LocalDate startDate,
        LocalDate endDate,
        String content,
        String phoneNumber,
        String homepageUrl,
        FestivalProgressStatus progressStatus,
        boolean wishlisted,
        String festivalCongestionLevel,
        RoadmapResponse roadmap,
        List<BoothResponse> booths
) {

    public static UserFestivalDetailResponse of(
            FestivalDetailView view,
            boolean wishlisted,
            String festivalCongestionLevel,
            RoadmapResponse roadmap,
            List<BoothResponse> booths
    ) {
        return new UserFestivalDetailResponse(
                view.festivalId(),
                view.name(),
                view.eventPlace(),
                view.address(),
                view.startDate(),
                view.endDate(),
                view.content(),
                view.phoneNumber(),
                view.homepageUrl(),
                view.progressStatus(),
                wishlisted,
                festivalCongestionLevel,
                roadmap,
                booths
        );
    }
}
