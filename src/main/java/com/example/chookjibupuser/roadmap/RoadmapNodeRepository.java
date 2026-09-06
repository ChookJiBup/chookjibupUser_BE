// roadmap/RoadmapNodeRepository.java
package com.example.chookjibupuser.roadmap;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {
    List<RoadmapNode> findByRoadmapIdOrderBySortOrderAsc(Long roadmapId);
}