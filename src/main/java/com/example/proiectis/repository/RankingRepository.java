package com.example.proiectis.repository;

import com.example.proiectis.entity.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    List<Ranking> findAllByOrderByTotalPointsDesc();
}