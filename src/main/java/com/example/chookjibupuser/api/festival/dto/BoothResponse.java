package com.example.chookjibupuser.api.festival.dto;

import com.example.chookjibupuser.booth.dto.BoothView;

/**
 * 부스 기본 정보는 축제 상태와 무관하게 항상 채워지고, congestion(혼잡도/대기시간)은
 * 축제가 진행중(ONGOING)일 때만 채워진다(그 외엔 null).
 */
public record BoothResponse(
        Long boothId,
        String boothName,
        String boothContent,
        String boothLocation,
        BoothCongestionResponse congestion
) {

    public static BoothResponse of(BoothView view, BoothCongestionResponse congestion) {
        return new BoothResponse(
                view.boothId(),
                view.boothName(),
                view.boothContent(),
                view.boothLocation(),
                congestion
        );
    }
}
