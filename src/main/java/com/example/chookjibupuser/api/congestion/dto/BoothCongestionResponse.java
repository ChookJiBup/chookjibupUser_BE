// api/congestion/dto/BoothCongestionResponse.java (신규)
package com.example.chookjibupuser.api.congestion.dto;

import com.example.chookjibupuser.congestion.BoothCongestionLevel;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 부스 하나의 최신 혼잡도. 이력이 아직 없으면 level/waitMinutes/updatedAt이 전부 null.
 *
 * @param roadmapNodePublicId 이 부스의 배치도 노드 식별자. 축제 상세 응답의
 *                            {@code roadmap.zones[].booths[].publicId}와 같은 값이라
 *                            지도 핀과 혼잡도를 부스 이름이 아니라 이 값으로 잇는다.
 *                            (관리자 대시보드 응답도 같은 이름으로 같은 값을 내려준다.)
 *                            지도에 찍히지 않은 부스는 null.
 * @param zoneId              묶여 있는 구역. 관리자가 구역으로 묶지 않았으면 null(구역 미지정)
 * @param zoneName            구역 이름. zoneId가 null이면 같이 null
 */
public record BoothCongestionResponse(
        Long boothId,
        String boothName,
        UUID roadmapNodePublicId,
        UUID zoneId,
        String zoneName,
        BoothCongestionLevel congestionLevel,
        Integer waitMinutes,
        LocalDateTime updatedAt
) {
}
