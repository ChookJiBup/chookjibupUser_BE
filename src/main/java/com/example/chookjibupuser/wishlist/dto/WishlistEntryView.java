package com.example.chookjibupuser.wishlist.dto;

import java.time.OffsetDateTime;

/**
 * 찜 도메인 내부에서만 쓰는 순수 뷰이다. festivalId만 갖고 있고 축제 상세 정보는
 * 모른다 — 상세 정보 병합은 wishlist 도메인의 책임이 아니라 api 계층(orchestration)의
 * 책임이다.
 */
public record WishlistEntryView(
        Long festivalId,
        OffsetDateTime wishlistedAt
) {
}
