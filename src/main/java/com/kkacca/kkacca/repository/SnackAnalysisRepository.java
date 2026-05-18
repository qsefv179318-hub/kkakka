package com.kkacca.kkacca.repository;

import com.kkacca.kkacca.entity.SnackAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface SnackAnalysisRepository extends JpaRepository<SnackAnalysis, Long> {

    @Query("SELECT a FROM SnackAnalysis a ORDER BY a.sweetScore DESC LIMIT 1")
    Optional<SnackAnalysis> findTopBySweet();

    @Query("SELECT a FROM SnackAnalysis a ORDER BY a.saltyScore DESC LIMIT 1")
    Optional<SnackAnalysis> findTopBySalty();

    @Query("SELECT a FROM SnackAnalysis a ORDER BY a.spicyScore DESC LIMIT 1")
    Optional<SnackAnalysis> findTopBySpicy();

    @Query("SELECT a FROM SnackAnalysis a ORDER BY a.crispyScore DESC LIMIT 1")
    Optional<SnackAnalysis> findTopByCrispy();

    @Query("SELECT a FROM SnackAnalysis a ORDER BY a.softScore DESC LIMIT 1")
    Optional<SnackAnalysis> findTopBySoft();
}
