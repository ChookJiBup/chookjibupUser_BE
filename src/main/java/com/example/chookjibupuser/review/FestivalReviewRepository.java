package com.example.chookjibupuser.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalReviewRepository extends JpaRepository<FestivalReview, Long> {

    Page<FestivalReview> findByFestivalIdOrderByReviewIdDesc(Long festivalId, Pageable pageable);

    /** 마이페이지 "내가 쓴 리뷰" 목록에서 쓴다. 현장(QR) 익명 리뷰는 userId가 null이라 여기 잡히지 않는다. */
    Page<FestivalReview> findByUserIdOrderByReviewIdDesc(Long userId, Pageable pageable);

    /** 축제별 리뷰 개수를 센다. 목록/상세 화면에 "리뷰 N개" 표시할 때 쓴다. */
    @org.springframework.data.jpa.repository.Query(
            "SELECT r.festivalId, COUNT(r) FROM FestivalReview r WHERE r.festivalId IN :festivalIds GROUP BY r.festivalId"
    )
    java.util.List<Object[]> countByFestivalIdIn(
            @org.springframework.data.repository.query.Param("festivalIds") java.util.List<Long> festivalIds
    );
}