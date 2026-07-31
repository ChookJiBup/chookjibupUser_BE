package com.example.chookjibupuser.wishlist;

import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.wishlist.dto.WishlistEntryPageView;
import com.example.chookjibupuser.wishlist.dto.WishlistEntryView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 찜 조회를 처리한다. wishlist 도메인 자신의 저장소만 다룬다 — 축제 상세 정보는
 * 절대 여기서 채우지 않는다(festivalId만 돌려준다). 축제 상세와 합치는 건
 * api 계층의 책임이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistQueryService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final WishlistRepository wishlistRepository;

    /**
     * 사용자가 찜한 festivalId 목록을 최신순으로 페이지 조회한다.
     */
    public WishlistEntryPageView getMyWishlist(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<FestivalWishlist> result = wishlistRepository.findByUserIdOrderByWishlistIdDesc(
                userId,
                pageable
        );

        return new WishlistEntryPageView(
                result.getContent().stream()
                        .map(w -> new WishlistEntryView(w.getFestivalId(), w.getCreatedAt()))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * 특정 축제를 사용자가 찜했는지 확인한다 (축제 상세 화면에서 쓴다).
     */
    public boolean isWishlisted(Long userId, Long festivalId) {
        if (userId == null) {
            return false;
        }
        return wishlistRepository.existsByUserIdAndFestivalId(userId, festivalId);
    }

    /**
     * 주어진 festivalId들 중, 사용자가 찜한 것만 골라 반환한다.
     */
    public Set<Long> getWishlistedFestivalIds(Long userId, List<Long> festivalIds) {
        if (userId == null || festivalIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(wishlistRepository.findFestivalIdByUserIdAndFestivalIdIn(userId, festivalIds));
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return size;
    }
}
