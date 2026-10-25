package com.datasphere.fusex.preguia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreGuiaRepository extends JpaRepository<PreGuiaModel, Long> {

    List<PreGuiaModel> findAllByOrderByIdAsc();

    List<PreGuiaModel> findByStatusOrderByIdAsc(StatusPreGuia status);

    List<PreGuiaModel> findByMedIdOrderByIdAsc(Integer medId);

    List<PreGuiaModel> findByMedIdAndStatusOrderByIdAsc(Integer medId, StatusPreGuia status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PreGuiaModel p where p.id = :id")
    Optional<PreGuiaModel> findByIdParaAtualizar(@Param("id") Long id);
}