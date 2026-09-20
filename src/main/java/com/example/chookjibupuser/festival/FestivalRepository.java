package com.example.chookjibupuser.festival;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    /**
     * 축제들의 대표 위치 좌표.
     *
     * <p>관리자 콘솔이 등록한 축제는 좌표를 `festival_locations`에 두고 `festivals`의
     * latitude/longitude는 비워 둔다. 축제마다 한 번씩 물으면 목록 한 페이지에 쿼리가
     * 수십 번 나가므로 한꺼번에 읽는다. 좌표가 한쪽만 채워진 행은 지도에 찍을 수 없어
     * 애초에 빼고 가져온다.</p>
     */
    @Query(value = """
            SELECT fl.festival_id AS "festivalId",
                   fl.location_id AS "locationId",
                   fl.latitude    AS "latitude",
                   fl.longitude   AS "longitude"
              FROM festival_locations fl
             WHERE fl.festival_id IN (:festivalIds)
               AND fl.is_primary = TRUE
               AND fl.latitude IS NOT NULL
               AND fl.longitude IS NOT NULL
            """, nativeQuery = true)
    List<FestivalPrimaryCoordinateRow> findPrimaryCoordinates(
            @Param("festivalIds") Collection<Long> festivalIds
    );


    Page<Festival> findByFestivalNameContainingIgnoreCaseOrderByStartDateAscFestivalIdAsc(
            String name, Pageable pageable
    );

    List<Festival> findByFestivalIdIn(List<Long> festivalIds);

    @Query(value = """
            SELECT f.* FROM festivals f
            LEFT JOIN (
                SELECT festival_id, COUNT(*) AS cnt
                FROM festival_wishlist
                GROUP BY festival_id
            ) w ON w.festival_id = f.festival_id
            LEFT JOIN (
                SELECT festival_id, COUNT(*) AS cnt
                FROM festival_review
                GROUP BY festival_id
            ) r ON r.festival_id = f.festival_id
            WHERE (CAST(:region AS text) IS NULL
                   OR f.road_address LIKE CONCAT('%', CAST(:region AS text), '%'))
              AND (
                    CAST(:status AS text) IS NULL
                    OR (:status = 'ONGOING'
                        AND f.start_date <= :today
                        AND f.end_date >= :today)
                    OR (:status = 'UPCOMING'
                        AND f.start_date > :today)
                    OR (:status = 'COMPLETED'
                        AND f.end_date < :today)
                  )
            ORDER BY
              CASE
                  WHEN :sort = 'VIEW_COUNT'
                  THEN COALESCE(f.view_count, 0)
              END DESC,

              CASE
                  WHEN :sort = 'WISHLIST_COUNT'
                  THEN COALESCE(w.cnt, 0)
              END DESC,

              CASE
                  WHEN :sort = 'REVIEW_COUNT'
                  THEN COALESCE(r.cnt, 0)
              END DESC,

              CASE
                  WHEN CAST(:sort AS text) IS NULL
                  THEN f.start_date
              END ASC,

              f.festival_id ASC
            """,
            countQuery = """
            SELECT count(*)
            FROM festivals f
            WHERE (CAST(:region AS text) IS NULL
                   OR f.road_address LIKE CONCAT('%', CAST(:region AS text), '%'))
              AND (
                    CAST(:status AS text) IS NULL
                    OR (:status = 'ONGOING'
                        AND f.start_date <= :today
                        AND f.end_date >= :today)
                    OR (:status = 'UPCOMING'
                        AND f.start_date > :today)
                    OR (:status = 'COMPLETED'
                        AND f.end_date < :today)
                  )
            """,
            nativeQuery = true)
    Page<Festival> findByRegionAndStatusAndSort(
            @Param("region") String region,
            @Param("status") String status,
            @Param("sort") String sort,
            @Param("today") LocalDate today,
            Pageable pageable
    );

    Optional<Festival> findByPublicId(UUID publicId);

    @Modifying
    @Query("""
            UPDATE Festival f
            SET f.viewCount = f.viewCount + 1
            WHERE f.festivalId = :festivalId
            """)
    int incrementViewCount(@Param("festivalId") Long festivalId);
}