// roadmap/FestivalRoadmap.java
package com.example.chookjibupuser.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 읽기 전용. status가 PUBLISHED일 때만 사용자에게 보여준다 (RoadmapQueryService 참고).
 */
@Entity
@Getter
@Table(name = "festival_roadmap")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalRoadmap {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "current_map_id")
    private Long currentMapId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private RoadmapStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "zones", columnDefinition = "jsonb")
    private List<RoadmapZone> zones;

    public List<RoadmapZone> getZones() {
        return Collections.unmodifiableList(zones == null ? List.of() : zones);
    }

}