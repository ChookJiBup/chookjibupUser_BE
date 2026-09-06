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
}