package com.example.chookjibupuser.roadmap;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {
    List<RoadmapNode> findByRoadmapIdOrderBySortOrderAsc(Long roadmapId);

    /** 부스↔지도 노드를 id로 이을 때 쓴다(내부 PK 여러 개 → 노드 엔티티 여러 개). */
    List<RoadmapNode> findByIdIn(Collection<Long> ids);
}