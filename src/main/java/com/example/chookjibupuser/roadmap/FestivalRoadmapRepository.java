// roadmap/FestivalRoadmapRepository.java
package com.example.chookjibupuser.roadmap;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalRoadmapRepository extends JpaRepository<FestivalRoadmap, Long> {
    Optional<FestivalRoadmap> findByFestivalId(Long festivalId);
}