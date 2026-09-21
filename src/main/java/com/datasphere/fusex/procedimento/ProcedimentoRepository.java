package com.datasphere.fusex.procedimento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcedimentoRepository extends JpaRepository<ProcedimentoModel, Integer> {
    public Optional<ProcedimentoModel> findById(Integer id);
}
