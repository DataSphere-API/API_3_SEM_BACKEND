package com.datasphere.fusex.guiaProc;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GuiaProcRepository extends JpaRepository <GuiaProcModel, Long> {

    List<GuiaProcModel> findByContratoModel_OcsModel_Cnpj(String cnpj);


    @EntityGraph(attributePaths = "contratoModel")
    List<GuiaProcModel> findByGuiaIdIn(Collection<Integer> guiaIds);

}