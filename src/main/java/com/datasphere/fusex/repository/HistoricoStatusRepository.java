package com.datasphare.fusex.repository;

import com.datasphare.fusex.domain.HistoricoStatus;
import com.datasphare.fusex.domain.enums.EntidadeTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    List<HistoricoStatus> findByEntidadeTipoAndEntidadeIdOrderByDataAsc(
            EntidadeTipo entidadeTipo, Long entidadeId);
}