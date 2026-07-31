package com.example.chookjibupuser.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로드맵 캔버스 위에 배치된 아이콘 한 건. {@code roadmap_icon_placement} 테이블에 매핑한다.
 */
@Entity
@Getter
@Table(name = "roadmap_icon_placement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapIconPlacement {

    @Id
    @Column(name = "placement_id")
    private Long placementId;

    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "icon_type_id")
    private Long iconTypeId;

    @Column(name = "related_booth_id")
    private Long relatedBoothId;

    @Column(name = "position_x")
    private BigDecimal positionX;

    @Column(name = "position_y")
    private BigDecimal positionY;

    @Column(name = "rotation_deg")
    private BigDecimal rotationDeg;

    @Column(name = "label")
    private String label;
}
