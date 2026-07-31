package com.example.chookjibupuser.api.wishlist.dto;

/**
 * 찜 토글(하트 클릭) 결과 응답이다. 실패 케이스가 없다 — 클릭하면 항상 이 응답이 오고,
 * wishlisted 값으로 최종 상태(찜됨/찜 취소됨)만 알려준다.
 */
public record WishlistToggleResponse(
        Long festivalId,
        boolean wishlisted
) {
}
