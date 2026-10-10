package com.datasphere.fusex.fatura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemFaturaRepository extends JpaRepository<ItemFaturaModel, Integer> {
    List<ItemFaturaModel> findByFaturaId(Long faturaId);
}