package com.datasphere.fusex.guia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GuiaRepository extends JpaRepository<GuiaModel, Integer> {

    List<GuiaModel> findByStatusInOrderByIdAsc(Collection<StatusGuia> status);
}