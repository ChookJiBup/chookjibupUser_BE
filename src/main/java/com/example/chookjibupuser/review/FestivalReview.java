package com.example.chookjibupuser.review;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 축제 리뷰(별점 + 한줄평) 한 건. 파이썬 파이프라인(schema.sql)이 이미 만들어둔
 * {@code festival_review} 테이블에 매핑한다 — 이 테이블은 애초에 리뷰 기능을 염두에 두고
 * 설계돼 있어서 스키마 변경 없이 그대로 쓴다.
 *
 * <p>review 도메인은 user/festival 엔티티를 전혀 모른다 — user_id/festival_id를 그냥
 * 숫자(Long)로만 들고 있는다 (wishlist 도메인과 동일한 패턴). QR코드에 담긴 건
 * festival의 public_id(UUID)인데, 그걸 festival_id(내부 PK)로 바꾸는 건 이 도메인이
 * 아니라 application 계층(UserReviewService)의 책임이다.</p>
 *
 * <p>rating은 DB가 SMALLINT라서 Java Integer가 아니라 Short로 매핑한다 — congestion_level/
 * TIMESTAMPTZ 때 겪었던 것과 같은 이유로, SQL 타입을 정확히 맞춰서 ddl-auto: validate
 * 실패 위험을 없앴다.</p>
 */
@Entity
@Getter
@Table(name = "festival_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "festival_id", nullable = false, updatable = false)
    private Long festivalId;

    @Column(name = "rating")
    private Short rating;

    @Column(name = "content", nullable = false)
    private String content;

    // DB 기본값(now())이 채워주므로 insertable=false로 두고 조회 시에만 읽어온다.
    // TIMESTAMPTZ라 OffsetDateTime으로 매핑한다.
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    private FestivalReview(Long userId, Long festivalId, Short rating, String content) {
        this.userId = userId;
        this.festivalId = festivalId;
        this.rating = rating;
        this.content = content;
    }

    public static FestivalReview create(Long userId, Long festivalId, int rating, String content) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating은 1~5 사이여야 합니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content는 필수입니다.");
        }
        return new FestivalReview(userId, festivalId, (short) rating, content);
    }
}
