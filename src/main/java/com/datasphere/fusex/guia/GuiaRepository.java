package com.datasphere.fusex.guia;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuiaRepository extends JpaRepository<GuiaModel, Long> {

    List<GuiaModel> findByBenfIdOrderByIdDesc(String benfId);

    List<GuiaModel> findByStatusAndDataEmissaoBefore(String status, LocalDate limite);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GuiaModel g where g.id = :id")
    Optional<GuiaModel> findByIdParaAtualizar(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("update GuiaModel g set g.status = :status where g.id = :id")
    int atualizarStatus(@Param("id") Long id, @Param("status") String status);
}