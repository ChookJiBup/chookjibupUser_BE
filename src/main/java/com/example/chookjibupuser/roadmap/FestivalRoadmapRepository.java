package com.example.chookjibupuser.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FestivalRoadmapRepository extends JpaRepository<FestivalRoadmap, Long> {

    @Query(value = "SELECT roadmap_type::text FROM festival_roadmap WHERE festival_id = :festivalId", nativeQuery = true)
    Optional<String> findRoadmapTypeText(@Param("festivalId") Long festivalId);
}
