// api/congestion/dto/FestivalCongestionResponse.java (신규)
package com.example.chookjibupuser.api.congestion.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 축제 전체 혼잡도 응답. ranking은 대기시간이 긴 순으로 정렬된 부스 목록
 * (대기시간 정보 없는 부스는 제외), booths는 전체 부스 목록이다.
 */
public record FestivalCongestionResponse(
        LocalDateTime updatedAt,
        Integer activeQueueCount,
        Integer averageWaitMinutes,
        List<BoothCongestionResponse> ranking,
        List<BoothCongestionResponse> booths
) {
}