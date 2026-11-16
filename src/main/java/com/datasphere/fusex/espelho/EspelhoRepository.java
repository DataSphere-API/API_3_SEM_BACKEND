package com.datasphere.fusex.espelho;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EspelhoRepository extends JpaRepository<EspelhoModel, Integer> {
    Optional<EspelhoModel> findByAtendimentoId(Long atendimentoId);
    List<EspelhoModel> findByOcsId(String ocsId);
    List<EspelhoModel> findByStatus(StatusEspelho status);

    interface EspelhoPendente {
        Integer getEspelhoId();
        String getStatus();
    }

    @Query(value = """
            select id as "espelhoId", status
            from espelho
            where ocs_id = :cnpj and data_inicio >= :inicio and data_inicio < :fim
              and status in ('EM_ABERTO', 'REPROVADO')
            order by id
            """, nativeQuery = true)
    List<EspelhoPendente> listarPendentesDaCompetencia(@Param("cnpj") String cnpj,
                                              @Param("inicio") LocalDate inicio,
                                              @Param("fim") LocalDate fim);
}
