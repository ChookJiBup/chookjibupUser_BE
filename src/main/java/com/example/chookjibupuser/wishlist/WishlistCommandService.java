package com.example.chookjibupuser.wishlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 찜 토글(하트 클릭)을 처리한다. wishlist 도메인 자신의 저장소만 다루고, 다른 도메인
 * (festival 등)에 대해서는 전혀 알지 못한다.
 *
 * <p>하트 아이콘 클릭 = 토글이라서, "이미 찜함"/"찜한 적 없음"은 실패가 아니라
 * 정상적인 두 상태일 뿐이다.</p>
 *
 * <p>동시에 두 번 클릭돼서 INSERT가 UNIQUE(user_id, festival_id) 제약에 걸리는
 * 극히 드문 레이스는 여기서 잡지 않는다 — 이 메서드 자체가 @Transactional
 * 경계라서, 예외를 여기 안에서 catch하면 이미 문제가 생긴 Hibernate 세션으로
 * 커밋을 시도하게 돼 오히려 다른 에러(UnexpectedRollbackException)로 이어질
 * 수 있다. 그래서 예외는 그대로 던져서 트랜잭션이 깨끗하게 롤백되게 하고,
 * 트랜잭션 밖에 있는 호출자(UserWishlistService)가 잡아서 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistCommandService {

    private final WishlistRepository wishlistRepository;

    /**
     * 찜 상태를 뒤집는다. 이미 찜했으면 취소하고, 안 했으면 찜한다.
     *
     * @return 토글 후 최종 상태 (true = 찜됨, false = 찜 취소됨)
     */
    public boolean toggle(Long userId, Long festivalId) {
        return wishlistRepository.findByUserIdAndFestivalId(userId, festivalId)
                .map(existing -> {
                    wishlistRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    wishlistRepository.save(FestivalWishlist.create(userId, festivalId));
                    return true;
                });
    }
}
