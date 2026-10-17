package com.datasphere.fusex.chefe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChefeFusexRepository extends JpaRepository<ChefeFusexModel, Long> {
}