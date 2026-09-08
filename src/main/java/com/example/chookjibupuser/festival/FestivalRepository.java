package com.example.chookjibupuser.festival;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    Page<Festival> findByFestivalNameContainingIgnoreCaseOrderByStartDateAscFestivalIdAsc(
            String name, Pageable pageable
    );

    List<Festival> findByFestivalIdIn(List<Long> festivalIds);

    /**
     * region(지역) + status(상태) + sort(정렬)를 전부 동시에 조합 가능한 통합 쿼리다.
     * 셋 다 선택적(null 허용)이고 서로 조합 가능하다 — "경기도의 진행중인 축제를
     * 찜 많은 순으로" 같은 조합이 자연스럽게 되어야 해서, 지역/상태/정렬을 각각 따로
     * 처리하던 쿼리 3개를 이 하나로 합쳤다. 필터를 아무것도 안 주면(전부 null) 축제
     * 전체를 시작일순으로 보여주는 기존 "기본 목록"과 동일하게 동작한다. name 검색만
     * 별도(검색 화면 전용)로 남겨뒀다.
     *
     * <p>region은 시/도 정식 명칭이 "강원도"→"강원특별자치도"처럼 계속 바뀌어서, 정확한
     * 접두어 매칭 대신 road_address 안에 그 지역명이 포함돼 있는지만 본다.</p>
     *
     * <p>정렬은 파라미터 하나로 여러 기준(WISHLIST_COUNT/REVIEW_COUNT/기본 시작일순) 중
     * 하나를 고르는 거라, CASE 식을 정렬 기준으로 써서 처리한다 — sort 값에 안 맞는
     * CASE는 매 행마다 전부 NULL이 되니, 실질적으로 딱 하나의 정렬 기준만 적용된다.</p>
     */
    @Query(value = """
            SELECT f.* FROM festivals f
            LEFT JOIN (
                SELECT festival_id, COUNT(*) AS cnt FROM festival_wishlist GROUP BY festival_id
            ) w ON w.festival_id = f.festival_id
            LEFT JOIN (
                SELECT festival_id, COUNT(*) AS cnt FROM festival_review GROUP BY festival_id
            ) r ON r.festival_id = f.festival_id
            WHERE (CAST(:region AS text) IS NULL OR f.road_address LIKE CONCAT('%', CAST(:region AS text), '%'))
              AND (
                    CAST(:status AS text) IS NULL
                    OR (:status = 'ONGOING' AND f.start_date <= :today AND f.end_date >= :today)
                    OR (:status = 'UPCOMING' AND f.start_date > :today)
                    OR (:status = 'COMPLETED' AND f.end_date < :today)
                  )
            ORDER BY
              CASE WHEN :sort = 'WISHLIST_COUNT' THEN COALESCE(w.cnt, 0) END DESC,
              CASE WHEN :sort = 'REVIEW_COUNT' THEN COALESCE(r.cnt, 0) END DESC,
              CASE WHEN CAST(:sort AS text) IS NULL THEN f.start_date END ASC,
              f.festival_id ASC
            """,
            countQuery = """
            SELECT count(*) FROM festivals f
            WHERE (CAST(:region AS text) IS NULL OR f.road_address LIKE CONCAT('%', CAST(:region AS text), '%'))
              AND (
                    CAST(:status AS text) IS NULL
                    OR (:status = 'ONGOING' AND f.start_date <= :today AND f.end_date >= :today)
                    OR (:status = 'UPCOMING' AND f.start_date > :today)
                    OR (:status = 'COMPLETED' AND f.end_date < :today)
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
}