// roadmap/FestivalMapPresentation.java
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
 * 관리자가 카카오맵 위에 맞춰 둔 표시 설정. 부지 경계 폴리곤과 팜플렛 오버레이다.
 *
 * <p>관리자 백엔드가 쓰는 {@code festival_map_presentation} 테이블을 읽기 전용으로 매핑한다.
 * 사용자 앱은 이 값을 그리기만 하므로 쓰기 메서드를 두지 않는다.</p>
 */
@Entity
@Getter
@Table(name = "festival_map_presentation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalMapPresentation {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "map_id")
    private Long mapId;

    @Column(name = "festival_id")
    private Long festivalId;

    /** {"geometryType":"POLYGON","schemaVersion":"2.0","points":[{"lat":..,"lng":..}]} 형태의 JSON. */
    @Column(name = "boundary_geometry", columnDefinition = "jsonb")
    private String boundaryGeometry;

    @Column(name = "overlay_image_key")
    private String overlayImageKey;

    @Column(name = "overlay_image_width")
    private Integer overlayImageWidth;

    @Column(name = "overlay_image_height")
    private Integer overlayImageHeight;

    @Column(name = "overlay_center_lat")
    private BigDecimal overlayCenterLat;

    @Column(name = "overlay_center_lng")
    private BigDecimal overlayCenterLng;

    @Column(name = "overlay_ground_width_m")
    private BigDecimal overlayGroundWidthMeters;

    @Column(name = "overlay_rotation_deg")
    private BigDecimal overlayRotationDegrees;

    @Column(name = "overlay_opacity")
    private BigDecimal overlayOpacity;

    @Column(name = "overlay_visible")
    private boolean overlayVisible;

    @Column(name = "clip_to_boundary")
    private boolean clipToBoundary;

    /** 이미지와 앵커가 모두 있어야 지도 위에 얹을 수 있다. */
    public boolean hasDisplayableOverlay() {
        return overlayVisible
                && overlayImageKey != null
                && overlayImageWidth != null
                && overlayImageWidth > 0
                && overlayImageHeight != null
                && overlayImageHeight > 0
                && overlayCenterLat != null
                && overlayCenterLng != null
                && overlayGroundWidthMeters != null
                && overlayRotationDegrees != null;
    }
}
