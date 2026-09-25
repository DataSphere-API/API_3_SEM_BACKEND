package com.datasphere.fusex.espelho;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspelhoRepository extends JpaRepository<EspelhoModel, Integer> {
    Optional<EspelhoModel> findByAtendimentoId(Long atendimentoId);
}