// roadmap/dto/BoothZoneView.java
package com.example.chookjibupuser.roadmap.dto;

import java.util.UUID;

/**
 * 부스 하나가 배치도에서 차지하는 자리. roadmap 도메인이 부스 이름을 전혀 보지 않고
 * 노드 id만으로 만들어 주는 값이다.
 *
 * @param nodeId       {@code booth_info.roadmap_node_id}와 같은 값(내부 PK)
 * @param nodePublicId 지도 응답의 노드 식별자(UUID). 지도 핀과 혼잡도를 잇는 키다.
 * @param zoneId       묶여 있는 구역. 관리자가 구역으로 묶지 않았으면 null(구역 미지정)
 * @param zoneName     구역 이름. zoneId가 null이면 같이 null
 */
public record BoothZoneView(
        Long nodeId,
        UUID nodePublicId,
        UUID zoneId,
        String zoneName
) {
}
