package com.example.chookjibupuser.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 축제 로드맵(1:1). {@code festival_roadmap} 테이블에 매핑한다.
 *
 * <p>{@code roadmap_type} 컬럼은 Postgres 네이티브 ENUM 타입이라, {@code congestion_level}과
 * 같은 이유로 엔티티 필드로 매핑하지 않았다 — {@link FestivalRoadmapRepository}의
 * 네이티브 쿼리에서 ::text로 캐스팅해서 읽는다.</p>
 */
@Entity
@Getter
@Table(name = "festival_roadmap")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalRoadmap {

    @Id
    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "base_image_url")
    private String baseImageUrl;

    @Column(name = "canvas_width")
    private Integer canvasWidth;

    @Column(name = "canvas_height")
    private Integer canvasHeight;
}
