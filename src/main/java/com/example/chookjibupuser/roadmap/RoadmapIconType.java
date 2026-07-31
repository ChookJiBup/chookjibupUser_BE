package com.example.chookjibupuser.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로드맵에 배치 가능한 아이콘 카탈로그(부스/화장실/주차장 등). {@code roadmap_icon_type}
 * 테이블에 매핑한다.
 */
@Entity
@Getter
@Table(name = "roadmap_icon_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapIconType {

    @Id
    @Column(name = "icon_type_id")
    private Long iconTypeId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "icon_image_url")
    private String iconImageUrl;
}
