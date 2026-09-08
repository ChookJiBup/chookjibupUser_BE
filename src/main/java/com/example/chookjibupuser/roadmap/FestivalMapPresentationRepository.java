// roadmap/FestivalMapPresentationRepository.java
package com.example.chookjibupuser.roadmap;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalMapPresentationRepository
        extends JpaRepository<FestivalMapPresentation, Long> {

    Optional<FestivalMapPresentation> findByMapId(Long mapId);
}
