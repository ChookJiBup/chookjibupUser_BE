// congestion/dto/BoothCongestionView.java (신규)
package com.example.chookjibupuser.congestion.dto;

import com.example.chookjibupuser.congestion.BoothCongestion;
import com.example.chookjibupuser.congestion.BoothCongestionLevel;
import java.time.LocalDateTime;

/** congestion 도메인의 순수한 뷰. 부스 이름 같은 booth 도메인 정보는 담지 않는다. */
public record BoothCongestionView(
        Long boothId,
        BoothCongestionLevel level,
        Integer waitMinutes,
        LocalDateTime updatedAt
) {

    public static BoothCongestionView of(BoothCongestion congestion) {
        return new BoothCongestionView(
                congestion.getBoothId(),
                congestion.getCongestionLevel(),
                congestion.getWaitMinutes(),
                congestion.getCreatedAt()
        );
    }
}