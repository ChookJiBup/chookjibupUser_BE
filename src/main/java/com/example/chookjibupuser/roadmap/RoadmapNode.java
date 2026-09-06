// roadmap/RoadmapNode.java
package com.example.chookjibupuser.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** review_status가 CONFIRMED인 것만 사용자에게 노출한다 (RoadmapQueryService 참고). */
@Entity
@Getter
@Table(name = "roadmap_node")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapNode {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "roadmap_id")
    private Long roadmapId;

    @Column(name = "map_id")
    private Long mapId;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", length = 30)
    private NodeType nodeType;

    @Column(name = "node_name", length = 150)
    private String nodeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "geometry_type", length = 20)
    private GeometryType geometryType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geometry_data", columnDefinition = "jsonb")
    private String geometryData;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "recognized_text", length = 500)
    private String recognizedText;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NodeSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 30)
    private NodeReviewStatus reviewStatus;

    @Column(name = "sort_order")
    private int sortOrder;

    public boolean isBooth() {
        return nodeType == NodeType.BOOTH;
    }

    public boolean isConfirmed() {
        return reviewStatus == NodeReviewStatus.CONFIRMED;
    }
}