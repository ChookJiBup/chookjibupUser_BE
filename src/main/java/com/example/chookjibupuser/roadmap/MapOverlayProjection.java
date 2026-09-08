// roadmap/MapOverlayProjection.java
package com.example.chookjibupuser.roadmap;

import com.example.chookjibupuser.roadmap.dto.PresentationView.LatLngView;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 팜플렛 앵커(중심 위경도 + 가로가 덮는 실거리 + 방위각)를 네 귀퉁이 위경도로 옮긴다.
 *
 * <p>관리자 백엔드의 {@code MapAnchorProjector}와 같은 식을 쓴다. 사용자 앱이 같은 자리에
 * 이미지를 얹으려면 계산이 한 글자도 달라서는 안 되므로, 부호와 반올림 자리수까지 맞췄다.
 * 남서/북동 bounds로 내리면 회전이 사라지기 때문에 네 귀퉁이를 그대로 준다.</p>
 */
public final class MapOverlayProjection {

    private static final double METERS_PER_DEGREE_LATITUDE = 111_320d;

    private MapOverlayProjection() {
    }

    public record Corners(
            LatLngView topLeft,
            LatLngView topRight,
            LatLngView bottomRight,
            LatLngView bottomLeft
    ) {
    }

    /** 앵커나 이미지 크기가 온전하지 않으면 null. 그리지 않는 편이 어긋난 자리에 얹는 것보다 낫다. */
    public static Corners corners(FestivalMapPresentation presentation) {
        if (!presentation.hasDisplayableOverlay()) {
            return null;
        }
        double centerLat = presentation.getOverlayCenterLat().doubleValue();
        double centerLng = presentation.getOverlayCenterLng().doubleValue();
        double groundWidth = presentation.getOverlayGroundWidthMeters().doubleValue();
        double groundHeight = groundWidth
                * ((double) presentation.getOverlayImageHeight()
                / (double) presentation.getOverlayImageWidth());
        double theta = Math.toRadians(presentation.getOverlayRotationDegrees().doubleValue());
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        double metersPerDegreeLng = METERS_PER_DEGREE_LATITUDE
                * Math.cos(Math.toRadians(centerLat));
        if (Math.abs(metersPerDegreeLng) < 1e-9) {
            return null;
        }

        LatLngView topLeft = project(0, 0, centerLat, centerLng, groundWidth, groundHeight,
                cos, sin, metersPerDegreeLng);
        LatLngView topRight = project(1, 0, centerLat, centerLng, groundWidth, groundHeight,
                cos, sin, metersPerDegreeLng);
        LatLngView bottomRight = project(1, 1, centerLat, centerLng, groundWidth, groundHeight,
                cos, sin, metersPerDegreeLng);
        LatLngView bottomLeft = project(0, 1, centerLat, centerLng, groundWidth, groundHeight,
                cos, sin, metersPerDegreeLng);
        if (topLeft == null || topRight == null || bottomRight == null || bottomLeft == null) {
            return null;
        }
        return new Corners(topLeft, topRight, bottomRight, bottomLeft);
    }

    private static LatLngView project(
            double x,
            double y,
            double centerLat,
            double centerLng,
            double groundWidth,
            double groundHeight,
            double cos,
            double sin,
            double metersPerDegreeLng
    ) {
        double dx = (x - 0.5) * groundWidth;
        // 이미지 y축은 아래로 자라지만 위도는 위로 자라므로 부호를 뒤집는다.
        double dy = -(y - 0.5) * groundHeight;
        double east = dx * cos + dy * sin;
        double north = -dx * sin + dy * cos;

        double latitude = centerLat + north / METERS_PER_DEGREE_LATITUDE;
        double longitude = centerLng + east / metersPerDegreeLng;
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
            return null;
        }
        return new LatLngView(
                BigDecimal.valueOf(latitude).setScale(7, RoundingMode.HALF_UP),
                BigDecimal.valueOf(longitude).setScale(7, RoundingMode.HALF_UP)
        );
    }
}
