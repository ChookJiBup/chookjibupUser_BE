// api/congestion/dto/FestivalCongestionResponse.java (신규)
package com.example.chookjibupuser.api.congestion.dto;

import com.example.chookjibupuser.congestion.BoothCongestionLevel;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 축제 전체 혼잡도 응답. ranking은 대기시간이 긴 순으로 정렬된 부스 목록
 * (대기시간 정보 없는 부스는 제외), booths는 전체 부스 목록이다.
 *
 * @param congestionLevel 축제 한 곳을 대표하는 혼잡도 등급. 부스 등급 중 가장 높은 값이다
 *                        ({@link BoothCongestionLevel#highest}). 등급이 매겨진 부스가
 *                        하나도 없으면 null이고, 화면은 「정보 없음」으로 표시한다.
 *                        부스 단위 등급과 같은 개념이라 이름도 같은 congestionLevel을 쓴다.
 */
public record FestivalCongestionResponse(
        LocalDateTime updatedAt,
        BoothCongestionLevel congestionLevel,
        Integer activeQueueCount,
        Integer averageWaitMinutes,
        List<BoothCongestionResponse> ranking,
        List<BoothCongestionResponse> booths
) {
}
