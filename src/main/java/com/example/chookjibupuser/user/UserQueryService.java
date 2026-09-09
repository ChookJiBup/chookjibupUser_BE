package com.example.chookjibupuser.user;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * user 도메인 자신의 저장소만 다룬다. 리뷰 작성자 닉네임 표시처럼 다른 도메인이
 * "이 userId의 닉네임이 뭔지" 알아야 할 때 이 서비스를 쓴다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserAccountRepository userAccountRepository;

    /**
     * @return userId -> 닉네임. 존재하지 않는 userId는 결과 맵에서 빠진다
     *         (탈퇴 등으로 계정이 사라진 경우 — 호출하는 쪽에서 기본 표시명으로 대체하면 된다).
     */
    public Map<Long, String> getNicknames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        userAccountRepository.findByUserIdIn(userIds)
                .forEach(user -> result.put(user.getUserId(), user.getNickname()));
        return result;
    }
}