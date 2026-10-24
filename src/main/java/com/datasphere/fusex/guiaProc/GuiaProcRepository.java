package com.datasphere.fusex.guiaProc;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface GuiaProcRepository extends JpaRepository<GuiaProcModel, Long> {

    GuiaProcModel findByQr(String qr);

    List<GuiaProcModel> findByContratoModel_OcsModel_Cnpj(String cnpj);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GuiaProcModel g where g.qr = :qr")
    Optional<GuiaProcModel> findByQrParaBaixa(@Param("qr") String qr);

    List<GuiaProcModel> findByGuiaModel_Id(Long guiaId);

    List<GuiaProcModel> findByGuiaModel_IdIn(Collection<Long> guiaIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GuiaProcModel g where g.guiaId = :id")
    Optional<GuiaProcModel> findByIdParaSessao(@Param("id") Long id);
}
