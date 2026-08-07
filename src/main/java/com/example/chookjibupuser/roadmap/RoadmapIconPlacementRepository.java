package com.example.chookjibupuser.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapIconPlacementRepository extends JpaRepository<RoadmapIconPlacement, Long> {

    List<RoadmapIconPlacement> findByFestivalId(Long festivalId);
}
