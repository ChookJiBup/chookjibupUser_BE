// roadmap/dto/PresentationView.java
package com.example.chookjibupuser.roadmap.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 지도 위에 그대로 그리면 되는 표시 설정.
 *
 * <p>관리자 화면과 같은 모양으로 내려, 사용자 앱이 좌표 변환을 다시 하지 않아도 되게 한다.
 * 팜플렛의 네 귀퉁이는 서버가 앵커에서 계산해 넣는다.</p>
 */
public record PresentationView(
        List<LatLngView> boundary,
        OverlayView overlay
) {

    public record LatLngView(BigDecimal lat, BigDecimal lng) {
    }

    public record OverlayView(
            String imageUrl,
            int imageWidth,
            int imageHeight,
            LatLngView topLeft,
            LatLngView topRight,
            LatLngView bottomRight,
            LatLngView bottomLeft,
            BigDecimal opacity,
            boolean clipToBoundary
    ) {
    }
}
