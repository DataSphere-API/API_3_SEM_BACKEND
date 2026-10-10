package com.datasphere.fusex.guiaProc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuiaProcRepository extends JpaRepository <GuiaProcModel, Long> {
    GuiaProcModel findByQr (String qr);

    List<GuiaProcModel> findByContratoModel_OcsModel_Cnpj(String cnpj);

}
