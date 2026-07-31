package com.example.chookjibupuser.wishlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 축제를 찜한 기록. 파이썬 파이프라인(schema.sql)이 만들어둔
 * {@code festival_wishlist} 테이블에 매핑한다 (UNIQUE(user_id, festival_id)로
 * DB 레벨에서도 중복 찜을 막고 있다).
 */
@Entity
@Getter
@Table(name = "festival_wishlist")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalWishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long wishlistId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "festival_id", nullable = false, updatable = false)
    private Long festivalId;

    // DB 컬럼 기본값(now())이 채워주므로 insertable=false로 두고 조회 시에만 읽어온다.
    // 컬럼 타입이 timestamptz라 OffsetDateTime으로 매핑한다 (LocalDateTime을 쓰면
    // Hibernate가 'timestamp'(시간대 없음)로 기대해서 ddl-auto: validate에서 타입 불일치가 난다).
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    private FestivalWishlist(Long userId, Long festivalId) {
        this.userId = userId;
        this.festivalId = festivalId;
    }

    public static FestivalWishlist create(Long userId, Long festivalId) {
        return new FestivalWishlist(userId, festivalId);
    }
}
