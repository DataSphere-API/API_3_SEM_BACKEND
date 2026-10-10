package com.datasphere.fusex.ocs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OcsRepository extends JpaRepository<OcsModel, String> {
}