package com.example.chookjibupuser.festival;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * findById / findAllById / existsById는 JpaRepository가 festival_id(PK) 기준으로 기본 제공한다.
 *
 * <p>목록/상세 조회는 전부 네이티브 쿼리다 — progress_status(Postgres 네이티브 ENUM)를
 * {@link FestivalRow}에서 설명한 이유로 안전하게 ::text 캐스팅해서 읽기 위해서다.</p>
 *
 * <p>[임시] name/region/status 필터는 일단 다시 뺐다 — 필터 파라미터가 전부 null인
 * 비회원 조회에서 서버 에러가 발생해서, 원인 파악 전까지는 필터 없는 단순 목록 조회로
 * 되돌려서 정상 동작부터 확보했다. 필터는 나중에 다시 추가할 예정.</p>
 */
public interface FestivalRepository extends JpaRepository<Festival, Long> {

    @Query(value = """
            SELECT festival_id, public_id, festival_name, event_place, road_address, start_date, end_date,
                   content, phone_number, homepage_url, progress_status::text AS progress_status
            FROM festivals
            ORDER BY start_date ASC NULLS LAST, festival_id ASC
            """,
            countQuery = "SELECT count(*) FROM festivals",
            nativeQuery = true)
    Page<FestivalRow> search(Pageable pageable);

    @Query(value = """
            SELECT festival_id, public_id, festival_name, event_place, road_address, start_date, end_date,
                   content, phone_number, homepage_url, progress_status::text AS progress_status
            FROM festivals
            WHERE festival_id = :festivalId
            """, nativeQuery = true)
    Optional<FestivalRow> findRowById(@Param("festivalId") Long festivalId);

    @Query(value = """
            SELECT festival_id, public_id, festival_name, event_place, road_address, start_date, end_date,
                   content, phone_number, homepage_url, progress_status::text AS progress_status
            FROM festivals
            WHERE festival_id IN (:festivalIds)
            """, nativeQuery = true)
    List<FestivalRow> findRowsByIds(@Param("festivalIds") List<Long> festivalIds);

    /**
     * 프론트 URL/QR코드에 담긴 public_id로 축제를 찾는다 (예: 리뷰 작성 화면).
     * public_id 컬럼은 Postgres 네이티브 UUID 타입이라 String이 아니라 java.util.UUID로
     * 받는다 — JDBC가 이건 표준 타입으로 인식해서 congestion_level 같은 캐스팅 문제가 없다.
     */
    Optional<Festival> findByPublicId(UUID publicId);
}
