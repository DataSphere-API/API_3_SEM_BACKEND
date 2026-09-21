package com.datasphere.fusex.repository;

import com.datasphere.fusex.model.GuiaProc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuiaProcRepository extends JpaRepository <GuiaProc, Long> {
    GuiaProc findByQr (String qr);
}
