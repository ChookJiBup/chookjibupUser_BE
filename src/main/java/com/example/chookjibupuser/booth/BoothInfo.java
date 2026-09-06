// booth/BoothInfo.java (신규)
package com.example.chookjibupuser.booth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 축제에 승인된 부스 마스터. 관리자 백엔드(chookjibupAdmin_BE)의 {@code booth_info}
 * 테이블을 읽기 전용으로 매핑한다 — 지도 노드(roadmap_node)가 승인된 뒤에만 생성된다.
 * 이 서버는 쓰지 않는다(읽기 전용) — 부스 등록/승인은 관리자 백엔드의 몫이다.
 */
@Entity
@Getter
@Table(name = "booth_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoothInfo {

    @Id
    @Column(name = "booth_id")
    private Long id;

    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "roadmap_node_id")
    private Long roadmapNodeId;

    @Column(name = "booth_name")
    private String boothName;

    @Column(name = "booth_content")
    private String boothContent;

    @Column(name = "booth_location")
    private String boothLocation;
}