package com.datasphere.fusex.contrato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<ContratoModel, Integer> {

    boolean existsByProcedimentoModel_IdAndOcsModel_Cnpj(Integer procedimentoId, String cnpj);

    @EntityGraph(attributePaths = {"ocsModel", "procedimentoModel"})
    List<ContratoModel> findAllByOrderByIdAsc();

    Optional<ContratoModel> findFirstByProcedimentoModel_IdAndOcsModel_CnpjOrderByIdAsc(Integer procedimentoId, String cnpj);
}
