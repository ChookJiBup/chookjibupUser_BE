package com.example.chookjibupuser.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalReviewRepository extends JpaRepository<FestivalReview, Long> {

    Page<FestivalReview> findByFestivalIdOrderByReviewIdDesc(Long festivalId, Pageable pageable);
}
