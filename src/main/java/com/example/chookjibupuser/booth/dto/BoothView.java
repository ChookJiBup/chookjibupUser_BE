package com.example.chookjibupuser.booth.dto;

import com.example.chookjibupuser.booth.BoothInfo;

/**
 * booth 도메인의 순수한 뷰이다. 혼잡도 정보는 담지 않는다 (그건 congestion
 * 도메인 소관이라 api 계층에서 조합할 때 별도로 얹는다).
 */
public record BoothView(
        Long boothId,
        String boothName,
        String boothContent,
        String boothLocation
) {

    public static BoothView of(BoothInfo booth) {
        return new BoothView(
                booth.getBoothId(),
                booth.getBoothName(),
                booth.getBoothContent(),
                booth.getBoothLocation()
        );
    }
}
