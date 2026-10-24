package com.datasphere.fusex.contrato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<ContratoModel, Integer> {

    // Contrato de um procedimento com uma OCS (pode haver mais de um: pega o mais antigo).
    Optional<ContratoModel> findFirstByProcedimentoModel_IdAndOcsModel_CnpjOrderByIdAsc(Integer procedimentoId, String cnpj);
}