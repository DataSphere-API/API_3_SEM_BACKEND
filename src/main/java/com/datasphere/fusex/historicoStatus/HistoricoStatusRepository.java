package com.datasphere.fusex.historicoStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatusModel, Long> {

    List<HistoricoStatusModel> findByEntidadeTipoAndEntidadeIdOrderByDataAsc(
            EntidadeTipo entidadeTipo, Long entidadeId);

    List<HistoricoStatusModel> findByEntidadeTipoAndEntidadeIdIn(
            EntidadeTipo entidadeTipo, Collection<Long> entidadeIds);
}