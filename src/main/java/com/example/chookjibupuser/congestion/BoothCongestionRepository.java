// congestion/BoothCongestionRepository.java (신규)
package com.example.chookjibupuser.congestion;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoothCongestionRepository extends JpaRepository<BoothCongestion, Long> {

    /**
     * 각 boothId별로 가장 최근(created_at 최댓값, 동률이면 congestion_id 최댓값) 행만
     * 골라서 반환한다 — 관리자 백엔드의 findLatestByBoothIds와 동일한 쿼리다.
     */
    @Query(
            value = """
                    SELECT bc.*
                    FROM booth_congestion bc
                    INNER JOIN (
                        SELECT booth_id, MAX(created_at) AS max_created
                        FROM booth_congestion
                        WHERE booth_id IN (:boothIds)
                        GROUP BY booth_id
                    ) latest
                      ON bc.booth_id = latest.booth_id AND bc.created_at = latest.max_created
                    INNER JOIN (
                        SELECT booth_id, created_at, MAX(congestion_id) AS max_id
                        FROM booth_congestion
                        WHERE booth_id IN (:boothIds)
                        GROUP BY booth_id, created_at
                    ) tie
                      ON bc.booth_id = tie.booth_id AND bc.created_at = tie.created_at
                     AND bc.congestion_id = tie.max_id
                    """,
            nativeQuery = true
    )
    List<BoothCongestion> findLatestByBoothIds(@Param("boothIds") Collection<Long> boothIds);
}