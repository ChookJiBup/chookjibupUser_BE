// congestion/BoothCongestion.java (신규)
package com.example.chookjibupuser.congestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 부스 혼잡 이력 한 건. 관리자 백엔드의 {@code booth_congestion} 테이블(append-only)을
 * 읽기 전용으로 매핑한다. "지금 혼잡도"는 booth_id별 가장 최근(created_at 최댓값) 행이다.
 */
@Entity
@Getter
@Table(name = "booth_congestion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoothCongestion {

    @Id
    @Column(name = "congestion_id")
    private Long id;

    @Column(name = "booth_id")
    private Long boothId;

    @Column(name = "wait_minutes")
    private Integer waitMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "congestion_level", length = 20)
    private BoothCongestionLevel congestionLevel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}