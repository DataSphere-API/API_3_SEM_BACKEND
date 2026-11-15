package com.datasphere.fusex.fatura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaturaRepository extends JpaRepository<FaturaModel, Long> {
    Optional<FaturaModel> findByOcsIdAndCompetenciaAndStatus(String ocsId, String competencia, StatusFatura status);
    List<FaturaModel> findByStatus(StatusFatura status);
}