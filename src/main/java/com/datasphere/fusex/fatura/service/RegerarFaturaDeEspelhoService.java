package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.request.FaturaResponse;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;

@Service
public class RegerarFaturaDeEspelhoService {

    private final FaturaRepository faturaRepository;
    private final ValidarEspelhoAlteradoAposGeracaoService validarEspelhoAlteradoAposGeracaoService;
    private final GerarFaturaDeEspelhoService gerarFaturaDeEspelhoService;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public RegerarFaturaDeEspelhoService(FaturaRepository faturaRepository,
                                         ValidarEspelhoAlteradoAposGeracaoService validarEspelhoAlteradoAposGeracaoService,
                                         GerarFaturaDeEspelhoService gerarFaturaDeEspelhoService,
                                         RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.faturaRepository = faturaRepository;
        this.validarEspelhoAlteradoAposGeracaoService = validarEspelhoAlteradoAposGeracaoService;
        this.gerarFaturaDeEspelhoService = gerarFaturaDeEspelhoService;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    /**
     * Regera uma fatura a partir do estado atual do espelho vinculado a ela.
     * A fatura antiga nunca é editada: ela é marcada como REGERADA e uma nova
     * fatura (com os itens recalculados a partir do espelho corrigido) é criada em seu lugar.
     *
     * Retorna null quando a fatura não existe, já foi regerada, ou o espelho
     * vinculado não foi alterado desde a geração (nada a regerar).
     */
    public FaturaResponse regerarFaturaDeEspelho(Long faturaId) {
        FaturaModel faturaAntiga = faturaRepository.findById(faturaId).orElse(null);
        if (faturaAntiga == null || faturaAntiga.getStatus() != StatusFatura.ATIVA) {
            return null;
        }

        boolean espelhoAlterado = validarEspelhoAlteradoAposGeracaoService.validarEspelhoAlteradoAposGeracao(faturaAntiga);
        if (!espelhoAlterado) {
            return null;
        }

        EspelhoModel espelho = faturaAntiga.getEspelhoModel();

        faturaAntiga.setStatus(StatusFatura.REGERADA);
        faturaRepository.save(faturaAntiga);
        registrarMudancaStatusService.executar(EntidadeTipo.FATURA, faturaAntiga.getId(),
                StatusFatura.ATIVA.name(), StatusFatura.REGERADA.name(), "regeracao-por-espelho-corrigido");

        FaturaResponse novaFatura = gerarFaturaDeEspelhoService.gerarFaturaDeEspelho(espelho, faturaAntiga.getId());
        registrarMudancaStatusService.executar(EntidadeTipo.FATURA, novaFatura.id(),
                null, StatusFatura.ATIVA.name(), "regeracao-por-espelho-corrigido");

        return novaFatura;
    }
}