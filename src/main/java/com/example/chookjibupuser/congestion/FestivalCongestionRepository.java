package com.example.chookjibupuser.congestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FestivalCongestionRepository extends JpaRepository<FestivalCongestion, Long> {

    /**
     * 축제의 가장 최근 혼잡도 한 건을 가져온다. congestion_level은 Postgres 네이티브
     * ENUM이라 ::text로 캐스팅한다.
     */
    @Query(value = """
            SELECT congestion_level::text AS congestionLevel, updated_at AS updatedAt
            FROM festival_congestion
            WHERE festival_id = :festivalId
            ORDER BY updated_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<FestivalCongestionProjection> findLatestByFestivalId(@Param("festivalId") Long festivalId);
}
