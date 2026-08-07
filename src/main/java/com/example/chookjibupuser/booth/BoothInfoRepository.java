package com.example.chookjibupuser.booth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoothInfoRepository extends JpaRepository<BoothInfo, Long> {

    List<BoothInfo> findByFestivalId(Long festivalId);
}
