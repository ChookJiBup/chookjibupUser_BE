package com.example.chookjibupuser.roadmap;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapIconPlacementRepository extends JpaRepository<RoadmapIconPlacement, Long> {

    List<RoadmapIconPlacement> findByFestivalId(Long festivalId);
}
