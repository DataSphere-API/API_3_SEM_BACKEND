package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.dto.FaturaMensalResumoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ListarFaturasMensaisService {

    private final FaturaRepository faturaRepository;
    private final MontarFaturaMensalService montarFaturaMensalService;

    public ListarFaturasMensaisService(FaturaRepository faturaRepository,
                                       MontarFaturaMensalService montarFaturaMensalService) {
        this.faturaRepository = faturaRepository;
        this.montarFaturaMensalService = montarFaturaMensalService;
    }

    // Filtros opcionais: CNPJ da OCS, competência (AAAA-MM) e status. Mais nova primeiro.
    @Transactional(readOnly = true)
    public List<FaturaMensalResumoResponse> listar(String cnpj, String competencia, StatusFatura status) {
        List<FaturaModel> faturas = new ArrayList<>(faturaRepository.findAll());
        faturas.sort(Comparator.comparing(FaturaModel::getId).reversed());

        List<FaturaMensalResumoResponse> resposta = new ArrayList<>();
        for (FaturaModel fatura : faturas) {
            if (cnpj != null && !cnpj.isBlank() && !cnpj.equals(fatura.getOcsId())) {
                continue;
            }
            if (competencia != null && !competencia.isBlank() && !competencia.equals(fatura.getCompetencia())) {
                continue;
            }
            if (status != null && status != fatura.getStatus()) {
                continue;
            }
            resposta.add(montarFaturaMensalService.montarResumo(fatura));
        }
        return resposta;
    }
}
