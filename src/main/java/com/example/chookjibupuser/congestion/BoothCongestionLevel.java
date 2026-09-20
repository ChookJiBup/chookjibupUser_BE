// congestion/BoothCongestionLevel.java (신규)
package com.example.chookjibupuser.congestion;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/** 관리자 백엔드의 BoothCongestionLevel과 동일하다. plain VARCHAR라 직접 매핑 가능. */
public enum BoothCongestionLevel {
    LOW,
    MEDIUM,
    HIGH;

    /**
     * 여러 부스 등급 중 가장 높은 등급.
     *
     * <p>축제 한 곳의 대표 혼잡도를 이 값으로 삼는다. 평균이 아니라 최댓값인 이유는
     * 방문객에게 「지금 이 축제에서 가장 나쁜 상황」을 알려 주는 표시이기 때문이고,
     * 무엇보다 프런트가 서버에 이 필드가 없던 동안 쓰던 규칙이 그대로 최댓값이라
     * 화면에 보이는 값이 달라지면 안 된다.</p>
     *
     * <p>선언 순서(LOW &lt; MEDIUM &lt; HIGH)가 곧 등급 순서라 enum 자연 순서로 비교한다 —
     * 순위표를 따로 두면 나중에 등급이 늘 때 한쪽만 고치게 된다.</p>
     *
     * @return 등급이 하나도 없으면(아직 아무도 갱신 안 함) null
     */
    public static BoothCongestionLevel highest(Collection<BoothCongestionLevel> levels) {
        return levels.stream()
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
