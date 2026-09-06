// api/congestion/dto/BoothCongestionResponse.java (신규)
package com.example.chookjibupuser.api.congestion.dto;

import com.example.chookjibupuser.congestion.BoothCongestionLevel;
import java.time.LocalDateTime;

/** 부스 하나의 최신 혼잡도. 이력이 아직 없으면 level/waitMinutes/updatedAt이 전부 null. */
public record BoothCongestionResponse(
        Long boothId,
        String boothName,
        BoothCongestionLevel congestionLevel,
        Integer waitMinutes,
        LocalDateTime updatedAt
) {
}