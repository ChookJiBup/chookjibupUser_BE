// roadmap/FestivalMapRepository.java
package com.example.chookjibupuser.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@code festival_maps}는 축제당 한 행이 아니라 지도 교체 이력을 쌓는 테이블이다.
 * 지도를 갈아끼우면 예전 판이 {@code REPLACED}로 남으므로 축제 단위 단건 조회
 * (findByFestivalId)는 절대 두면 안 된다 — 이력이 두 장 이상인 축제에서 결과 개수
 * 예외로 터진다. 지금 쓰는 지도는 {@code festival_roadmap.current_map_id}로 집는다.
 */
public interface FestivalMapRepository extends JpaRepository<FestivalMap, Long> {
}
