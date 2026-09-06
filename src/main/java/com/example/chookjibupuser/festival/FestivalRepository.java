// festival/FestivalRepository.java (전체)
package com.example.chookjibupuser.festival;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    Page<Festival> findAllByOrderByStartDateAscFestivalIdAsc(Pageable pageable);

    Page<Festival> findByFestivalNameContainingIgnoreCaseOrderByStartDateAscFestivalIdAsc(
            String name, Pageable pageable
    );

    Page<Festival> findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAscFestivalIdAsc(
            LocalDate today1, LocalDate today2, Pageable pageable
    );

    Page<Festival> findByStartDateAfterOrderByStartDateAscFestivalIdAsc(LocalDate today, Pageable pageable);

    Page<Festival> findByEndDateBeforeOrderByStartDateAscFestivalIdAsc(LocalDate today, Pageable pageable);

    /**
     * 찜(위시리스트) 많은 순으로 정렬한다. festival 도메인은 wishlist 도메인의 자바
     * 엔티티를 직접 참조하면 안 되므로(도메인 독립성), JPQL이 아니라 네이티브 SQL로
     * 테이블 이름만 참조한다.
     */
    @Query(value = """
            SELECT f.* FROM festivals f
            LEFT JOIN (
                SELECT festival_id, COUNT(*) AS cnt FROM festival_wishlist GROUP BY festival_id
            ) w ON w.festival_id = f.festival_id
            ORDER BY COALESCE(w.cnt, 0) DESC, f.festival_id ASC
            """,
            countQuery = "SELECT count(*) FROM festivals",
            nativeQuery = true)
    Page<Festival> findAllOrderByWishlistCountDesc(Pageable pageable);

    /** 리뷰 많은 순으로 정렬한다. 위와 같은 이유로 네이티브 SQL을 쓴다. */
    @Query(value = """
            SELECT f.* FROM festivals f
            LEFT JOIN (
                SELECT festival_id, COUNT(*) AS cnt FROM festival_review GROUP BY festival_id
            ) r ON r.festival_id = f.festival_id
            ORDER BY COALESCE(r.cnt, 0) DESC, f.festival_id ASC
            """,
            countQuery = "SELECT count(*) FROM festivals",
            nativeQuery = true)
    Page<Festival> findAllOrderByReviewCountDesc(Pageable pageable);

    List<Festival> findByFestivalIdIn(List<Long> festivalIds);

    Optional<Festival> findByPublicId(UUID publicId);
}