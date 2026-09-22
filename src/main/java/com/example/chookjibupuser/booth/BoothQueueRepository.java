package com.example.chookjibupuser.booth;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoothQueueRepository extends JpaRepository<BoothQueue, Long> {

    List<BoothQueue> findByBoothIdIn(Collection<Long> boothIds);
}