package com.example.chookjibupuser.api.congestion.dto;

import com.example.chookjibupuser.congestion.BoothCongestionLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 부스 하나의 최신 혼잡도 + 대기열(줄) 상태.
 *
 * <p>혼잡도 이력이 아직 없으면 congestionLevel/waitMinutes/updatedAt이 전부 null이고,
 * 관리자가 아직 줄끝을 안 그렸으면 queueTailLatitude 이하 줄 관련 필드가 전부 null이다
 * — 두 그룹은 서로 다른 테이블(각각 booth_congestion, booth_queue)에서 오기 때문에
 * 독립적으로 null일 수 있다.</p>
 *
 * <p>roadmapNodePublicId는 이 부스가 찍힌 배치도 노드(roadmap_node)의 공개 UUID다.
 * 프론트가 배치도 응답(roadmap.zones[].booths[].publicId)과 이 혼잡도 응답을 이을 때
 * 부스 이름 대신 이 값으로 잇는다 — 이름이 겹치는 부스가 있으면 이름 매칭은 깨진다.
 * 배치도 노드에 아직 연결이 안 된 부스(드묾)면 null이다.</p>
 */
public record BoothCongestionResponse(
        Long boothId,
        String boothName,
        UUID roadmapNodePublicId,
        BoothCongestionLevel congestionLevel,
        Integer waitMinutes,
        LocalDateTime updatedAt,
        BigDecimal queueTailLatitude,
        BigDecimal queueTailLongitude,
        /** 줄 길이(미터). 대기열이 뻗어나간 거리다. */
        Integer queueTailMeters,
        /** 줄이 그려진 경로(위경도 점들)의 원문 JSON. 프론트에서 직접 파싱해서 지도 위에 그린다. */
        String queuePath,
        LocalDateTime queueUpdatedAt
) {
}