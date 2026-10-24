package com.datasphere.fusex.preguia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreGuiaProcRepository extends JpaRepository<PreGuiaProcModel, Long> {

    List<PreGuiaProcModel> findByPreGuiaModel_IdOrderByIdAsc(Long preGuiaId);
}