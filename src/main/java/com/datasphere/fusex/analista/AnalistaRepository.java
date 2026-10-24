package com.datasphere.fusex.analista;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalistaRepository extends JpaRepository<AnalistaModel, Integer> {

    Optional<AnalistaModel> findByBenefId(String benefId);
}