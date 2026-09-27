package com.datasphere.fusex.fatura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaturaRepository extends JpaRepository<FaturaModel, Long> {
    List<FaturaModel> findByStatus(StatusFatura status);
}