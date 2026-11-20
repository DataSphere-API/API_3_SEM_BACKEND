package com.datasphere.fusex.ocs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface OcsRepository extends JpaRepository<OcsModel, String> {
    @Modifying
    @Query(value = "INSERT INTO ocs (cnpj, nome) VALUES (:cnpj, :nome)", nativeQuery = true)
    void inserir(@Param("cnpj") String cnpj, @Param("nome") String nome);
}
