package com.datasphere.fusex.espelhoItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspelhoItemRepository extends JpaRepository<EspelhoItemModel, Integer> {
    List<EspelhoItemModel> findByEspelhoId(Integer espelhoId);
    Optional<EspelhoItemModel> findByIdAndEspelhoId(Integer id, Integer espelhoId);
}