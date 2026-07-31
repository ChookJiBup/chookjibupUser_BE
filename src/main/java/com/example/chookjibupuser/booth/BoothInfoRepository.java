package com.example.chookjibupuser.booth;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoothInfoRepository extends JpaRepository<BoothInfo, Long> {

    List<BoothInfo> findByFestivalId(Long festivalId);
}
