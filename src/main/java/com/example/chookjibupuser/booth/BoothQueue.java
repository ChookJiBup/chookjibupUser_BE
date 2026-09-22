package com.example.chookjibupuser.booth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 부스 대기열(줄끝) 현재 상태 한 건. 관리자 백엔드(chookjibupAdmin_BE)의
 * {@code booth_queue} 테이블을 읽기 전용으로 매핑한다 — 부스당 최대 1행이다.
 * "줄 길이"(대기열이 뻗어나간 거리)와 줄이 그려진 경로(path)를 담고 있다.
 * 이 서버는 쓰지 않는다(읽기 전용) — 줄끝 갱신은 관리자/현장 스태프의 몫이다.
 *
 * <p>대기시간/혼잡도(wait_minutes, congestion_level)도 이 테이블에 같이 있지만,
 * "지금 혼잡도" 조회는 기존처럼 이력 테이블(BoothCongestion 참고)을 그대로 쓴다 —
 * 이 엔티티는 booth_congestion에는 없는 줄 길이/경로 값만 담당한다.</p>
 */
@Entity
@Getter
@Table(name = "booth_queue")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoothQueue {

    @Id
    @Column(name = "queue_id")
    private Long id;

    @Column(name = "booth_id")
    private Long boothId;

    @Column(name = "tail_latitude")
    private BigDecimal tailLatitude;

    @Column(name = "tail_longitude")
    private BigDecimal tailLongitude;

    @Column(name = "queue_tail_meters")
    private Integer queueTailMeters;

    // 관리자 백엔드는 List<Map<String,BigDecimal>>로 다루지만, 이 서버는 다른 JSONB
    // 컬럼(roadmap_node.geometry_data 등)과 같은 방식으로 원문 JSON 문자열로만 받아서
    // 그대로 내려준다 — 파싱은 프론트가 한다(RoadmapNode.geometryData와 동일한 패턴).
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "path_geometry", columnDefinition = "jsonb")
    private String pathGeometry;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}