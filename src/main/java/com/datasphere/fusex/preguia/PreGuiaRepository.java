package com.datasphere.fusex.preguia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreGuiaRepository extends JpaRepository<PreGuiaModel, Long> {
}