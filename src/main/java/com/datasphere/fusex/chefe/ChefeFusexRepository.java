package com.datasphere.fusex.chefe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChefeFusexRepository extends JpaRepository<ChefeFusexModel, Long> {

    Optional<ChefeFusexModel> findByBenefId(String benefId);
}