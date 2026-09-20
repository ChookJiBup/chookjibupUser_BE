// booth/dto/BoothView.java (신규)
package com.example.chookjibupuser.booth.dto;

import com.example.chookjibupuser.booth.BoothInfo;

/**
 * @param roadmapNodeId 이 부스가 지도에서 어느 노드인지. 배치도(roadmap) 쪽 정보를 붙이려면
 *                      이 값이 유일한 실제 키다 — 부스 이름으로 맞춰 잇지 않기 위해 들고 나온다.
 *                      지도에 찍히지 않은 부스는 null.
 */
public record BoothView(
        Long boothId,
        String name,
        String content,
        String location,
        Long roadmapNodeId
) {

    public static BoothView of(BoothInfo booth) {
        return new BoothView(
                booth.getId(),
                booth.getBoothName(),
                booth.getBoothContent(),
                booth.getBoothLocation(),
                booth.getRoadmapNodeId()
        );
    }
}
