package com.example.chookjibupuser.wishlist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<FestivalWishlist, Long> {

    boolean existsByUserIdAndFestivalId(Long userId, Long festivalId);

    Optional<FestivalWishlist> findByUserIdAndFestivalId(Long userId, Long festivalId);

    Page<FestivalWishlist> findByUserIdOrderByWishlistIdDesc(Long userId, Pageable pageable);

    // 축제 목록 화면에서 항목마다 "찜했는지" 표시할 때 쓴다 (festivalId만 뽑아서 Set으로 비교).
    List<Long> findFestivalIdByUserIdAndFestivalIdIn(Long userId, List<Long> festivalIds);
}
