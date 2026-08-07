package com.example.chookjibupuser.congestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 축제 전체 혼잡도 이력. {@code festival_congestion} 테이블에 매핑한다.
 * congestion_level을 엔티티 필드로 매핑하지 않는 이유는 {@link BoothCongestion}과 동일하다
 * (Postgres 네이티브 ENUM — {@link FestivalCongestionRepository}의 네이티브 쿼리 참고).
 */
@Entity
@Getter
@Table(name = "festival_congestion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalCongestion {

    @Id
    @Column(name = "congestion_id")
    private Long congestionId;

    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
