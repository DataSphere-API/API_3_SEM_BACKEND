package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.fatura.FaturaModel;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
public class ListarEspelhosPendentesService {

    private final EspelhoRepository espelhoRepository;

    public ListarEspelhosPendentesService(EspelhoRepository espelhoRepository) {
        this.espelhoRepository = espelhoRepository;
    }

    // Espelhos da mesma OCS e do mesmo mês que ainda estão na auditoria ou em correção pela OCS.
    public List<EspelhoModel> listar(FaturaModel fatura) {
        if (fatura.getOcsId() == null || fatura.getCompetencia() == null) {
            return List.of();
        }
        YearMonth mes = YearMonth.parse(fatura.getCompetencia());
        return espelhoRepository.findByOcsIdAndDataInicioBetweenAndStatusIn(
                fatura.getOcsId(), mes.atDay(1), mes.atEndOfMonth(),
                List.of(StatusEspelho.EM_ABERTO, StatusEspelho.REPROVADO));
    }
}
