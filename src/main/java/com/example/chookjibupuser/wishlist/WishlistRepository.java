package com.example.chookjibupuser.wishlist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<FestivalWishlist, Long> {

    boolean existsByUserIdAndFestivalId(Long userId, Long festivalId);

    Optional<FestivalWishlist> findByUserIdAndFestivalId(Long userId, Long festivalId);

    Page<FestivalWishlist> findByUserIdOrderByWishlistIdDesc(Long userId, Pageable pageable);

    // 축제 목록 화면에서 항목마다 "찜했는지" 표시할 때 쓴다.
    // [수정] 메서드 이름 기반 파생 쿼리(findByXxxIn)로 단일 컬럼(festivalId)만 List<Long>으로
    // 반환하면, Spring Data가 내부적으로 결과를 변환하는 과정에서
    // ConversionFailedException(ArrayList<?> → List<Long>)이 나는 알려진 문제가 있다.
    // @Query로 JPQL을 직접 써서 이 변환 단계 자체를 피한다.
    @Query("SELECT w.festivalId FROM FestivalWishlist w WHERE w.userId = :userId AND w.festivalId IN :festivalIds")
    List<Long> findFestivalIdByUserIdAndFestivalIdIn(
            @Param("userId") Long userId,
            @Param("festivalIds") List<Long> festivalIds
    );

    /**
     * 축제별 찜(하트) 개수를 센다. 목록/상세 화면에 "N명이 찜함" 표시할 때 쓴다.
     * Object[0]=festivalId(Long), Object[1]=count(Long) 형태로 온다 — 단일 컬럼
     * List<Long> 변환 문제(위 주석 참고)와 달리 이런 다중 컬럼 집계는 Object[]로
     * 받으면 문제없이 동작한다.
     */
    @Query("SELECT w.festivalId, COUNT(w) FROM FestivalWishlist w WHERE w.festivalId IN :festivalIds GROUP BY w.festivalId")
    List<Object[]> countByFestivalIdIn(@Param("festivalIds") List<Long> festivalIds);

    /** 찜 목록 편집 모드(다중 선택 삭제)에서 쓴다. userId 조건을 항상 같이 걸어서 남의 찜은 못 지운다. */
    void deleteByUserIdAndFestivalIdIn(Long userId, List<Long> festivalIds);
}