// roadmap/FestivalMapRepository.java
package com.example.chookjibupuser.roadmap;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalMapRepository extends JpaRepository<FestivalMap, Long> {
    Optional<FestivalMap> findByFestivalId(Long festivalId);
}