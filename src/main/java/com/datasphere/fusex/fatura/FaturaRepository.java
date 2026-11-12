package com.datasphere.fusex.fatura;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaturaRepository extends JpaRepository<FaturaModel, Long> {
    List<FaturaModel> findByStatus(StatusFatura status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select fatura from FaturaModel fatura where fatura.id = :id")
    Optional<FaturaModel> findByIdParaAlterarStatus(@Param("id") Long id);
}