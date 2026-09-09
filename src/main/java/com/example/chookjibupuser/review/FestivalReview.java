package com.example.chookjibupuser.review;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 축제 리뷰(별점 + 한줄평) 한 건. {@code festival_review} 테이블에 매핑한다.
 *
 * <p>review 도메인은 user/festival 엔티티를 전혀 모른다 — user_id/festival_id를 그냥
 * 숫자(Long)로만 들고 있는다. QR코드에 담긴 건 festival의 public_id(UUID)인데, 그걸
 * festival_id(내부 PK)로 바꾸는 건 application 계층(UserReviewService)의 책임이다.</p>
 *
 * <p>[추가] 축제 현장 QR코드로 들어와서 남긴 "현장 리뷰"는 로그인 없이도 작성할 수 있게
 * 하기로 해서, user_id를 nullable로 바꿨다({@code isOnsite=true}일 때만 null 허용).
 * is_onsite 컬럼(boolean, not null, default false)을 새로 추가해서 일반 리뷰와
 * 구분한다 — 상세페이지에서 이 값으로 "인증 배지"를 보여준다.</p>
 *
 * <p>rating은 DB가 SMALLINT라서 Java Integer가 아니라 Short로 매핑한다.</p>
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

    // [수정] 현장 리뷰(QR, 비로그인)는 작성자가 없을 수 있어서 nullable로 바꿨다.
    @Column(name = "user_id", updatable = false)
    private Long userId;

    @Column(name = "festival_id", nullable = false, updatable = false)
    private Long festivalId;

    @Column(name = "rating")
    private Short rating;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_onsite", nullable = false, updatable = false)
    private boolean onsite;

    // DB 기본값(now())이 채워주므로 insertable=false로 두고 조회 시에만 읽어온다.
    // TIMESTAMPTZ라 OffsetDateTime으로 매핑한다.
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    private FestivalReview(Long userId, Long festivalId, Short rating, String content, boolean onsite) {
        this.userId = userId;
        this.festivalId = festivalId;
        this.rating = rating;
        this.content = content;
        this.onsite = onsite;
    }

    /**
     * @param userId  로그인한 작성자. 현장(QR) 리뷰이면서 비로그인이면 null이 올 수 있다.
     * @param onsite  축제 현장 QR코드로 들어와서 남긴 리뷰인지 여부.
     */
    public static FestivalReview create(Long userId, Long festivalId, int rating, String content, boolean onsite) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating은 1~5 사이여야 합니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content는 필수입니다.");
        }
        // 익명(비로그인) 작성은 반드시 현장 리뷰여야 한다 — 일반 리뷰는 로그인 필수.
        if (userId == null && !onsite) {
            throw new IllegalArgumentException("로그인하지 않은 사용자는 현장 리뷰만 작성할 수 있습니다.");
        }
        return new FestivalReview(userId, festivalId, (short) rating, content, onsite);
    }
}