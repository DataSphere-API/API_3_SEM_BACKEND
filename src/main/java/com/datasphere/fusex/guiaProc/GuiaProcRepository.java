package com.datasphere.fusex.guiaProc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuiaProcRepository extends JpaRepository <GuiaProcModel, Long> {
    GuiaProcModel findByQr (String qr);
}
