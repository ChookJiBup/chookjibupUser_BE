package com.example.chookjibupuser.congestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoothCongestionRepository extends JpaRepository<BoothCongestion, Long> {

    /**
     * 주어진 부스들의 "가장 최근" 혼잡도 한 건씩만 가져온다 (부스마다 이력이 여러 건
     * 쌓이기 때문에 Postgres의 DISTINCT ON으로 부스별 최신 1건만 뽑는다).
     * congestion_level은 Postgres 네이티브 ENUM이라 ::text로 캐스팅해서 문자열로 받는다.
     */
    @Query(value = """
            SELECT DISTINCT ON (booth_id)
                booth_id AS boothId,
                congestion_level::text AS congestionLevel,
                wait_minutes AS waitMinutes,
                updated_at AS updatedAt
            FROM booth_congestion
            WHERE booth_id IN (:boothIds)
            ORDER BY booth_id, updated_at DESC
            """, nativeQuery = true)
    List<BoothCongestionProjection> findLatestByBoothIds(@Param("boothIds") List<Long> boothIds);
}
