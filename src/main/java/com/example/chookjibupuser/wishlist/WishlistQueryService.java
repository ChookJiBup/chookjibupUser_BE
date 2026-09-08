package com.example.chookjibupuser.wishlist;

import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.wishlist.dto.WishlistEntryPageView;
import com.example.chookjibupuser.wishlist.dto.WishlistEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistQueryService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final WishlistRepository wishlistRepository;

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

    public boolean isWishlisted(Long userId, Long festivalId) {
        if (userId == null) {
            return false;
        }
        return wishlistRepository.existsByUserIdAndFestivalId(userId, festivalId);
    }

    public Set<Long> getWishlistedFestivalIds(Long userId, List<Long> festivalIds) {
        if (userId == null || festivalIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(wishlistRepository.findFestivalIdByUserIdAndFestivalIdIn(userId, festivalIds));
    }

    /**
     * 주어진 festivalId들 각각의 찜(하트) 개수를 반환한다. 찜이 하나도 없는 festivalId는
     * 결과 맵에서 빠진다 — 호출하는 쪽에서 없으면 0으로 취급하면 된다.
     */
    public java.util.Map<Long, Long> getWishlistCounts(List<Long> festivalIds) {
        if (festivalIds.isEmpty()) {
            return java.util.Map.of();
        }
        java.util.Map<Long, Long> result = new java.util.LinkedHashMap<>();
        for (Object[] row : wishlistRepository.countByFestivalIdIn(festivalIds)) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
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