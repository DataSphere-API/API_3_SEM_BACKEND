package com.datasphere.fusex.repository;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuiaProcRepository extends JpaRepository<GuiaProcModel, Long> {

    Optional<GuiaProcModel> findByQr(String qr);
}