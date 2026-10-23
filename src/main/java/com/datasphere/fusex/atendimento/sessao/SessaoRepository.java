package com.datasphere.fusex.atendimento.sessao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessaoRepository extends JpaRepository<SessaoModel, Long> {

    @Query("select max(s.numeroSessao) from SessaoModel s where s.guiaProc.guiaId = :guiaProcId")
    Optional<Integer> findMaxNumeroSessaoByGuiaProcId(@Param("guiaProcId") Long guiaProcId);

    boolean existsByGuiaProc_GuiaId(Long guiaProcId);
}