package com.datasphere.fusex.espelho;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EspelhoRepository extends JpaRepository<EspelhoModel, Integer> {
    Optional<EspelhoModel> findByAtendimentoId(Long atendimentoId);
    List<EspelhoModel> findByOcsId(String ocsId);
    List<EspelhoModel> findByStatus(StatusEspelho status);
    List<EspelhoModel> findByOcsIdAndDataInicioBetweenAndStatusIn(String ocsId, LocalDate inicio, LocalDate fim,
                                                                 List<StatusEspelho> status);
}