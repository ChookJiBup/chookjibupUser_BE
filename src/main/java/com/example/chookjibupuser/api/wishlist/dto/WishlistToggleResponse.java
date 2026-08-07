package com.example.chookjibupuser.api.wishlist.dto;

import java.util.UUID;

/**
 * 찜 토글(하트 클릭) 결과 응답이다. 실패 케이스가 없다 — 클릭하면 항상 이 응답이 오고,
 * wishlisted 값으로 최종 상태(찜됨/찜 취소됨)만 알려준다.
 *
 * <p>{@code festivalPublicId}는 요청으로 받은 값을 그대로 돌려주는 것이다 —
 * festivalId(내부 PK)는 응답에 노출하지 않는다.</p>
 */
public record WishlistToggleResponse(
        UUID festivalPublicId,
        boolean wishlisted
) {
}
