package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.dto.request.ValidacaoPreGuiaResponse;
import com.datasphere.fusex.guia.dto.request.ValidarPreGuiaResponse;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidarPreGuiaService {

    public static class PreGuiaNaoEncontradaException extends RuntimeException {
        public PreGuiaNaoEncontradaException(Long id) {
            super("Pré-guia " + id + " não encontrada");
        }
    }

    public static class PreGuiaStatusInvalidoException extends RuntimeException {
        public PreGuiaStatusInvalidoException(Long id, String status) {
            super("Pré-guia " + id + " está com status " + status + " e não pode ser validada");
        }
    }

    private static final Set<String> STATUS_VALIDAVEIS = Set.of("PRE_GUIA", "COM_PENDENCIAS");

    private final GuiaRepository guiaRepository;
    private final PreGuiaValidacaoService preGuiaValidacaoService;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public ValidarPreGuiaService(GuiaRepository guiaRepository,
                                 PreGuiaValidacaoService preGuiaValidacaoService,
                                 RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.guiaRepository = guiaRepository;
        this.preGuiaValidacaoService = preGuiaValidacaoService;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public ValidarPreGuiaResponse validarPreGuia(Long id) {
        GuiaModel guia = guiaRepository.findByIdParaAtualizar(id)
                .orElseThrow(() -> new PreGuiaNaoEncontradaException(id));
        String statusAtual = guia.getStatus();
        if (statusAtual == null || !STATUS_VALIDAVEIS.contains(statusAtual)) {
            throw new PreGuiaStatusInvalidoException(id, statusAtual);
        }

        ValidacaoPreGuiaResponse validacao = preGuiaValidacaoService.consolidarPendencias(guia);
        String novoStatus = validacao.validada() ? "VALIDADA" : "COM_PENDENCIAS";

        guia.setStatus(novoStatus);
        guiaRepository.save(guia);
        registrarMudancaStatusService.executar(EntidadeTipo.GUIA, id,
                statusAtual, novoStatus, "validacao-pre-guia");

        return new ValidarPreGuiaResponse(id, novoStatus, validacao.validada(), validacao.pendencias());
    }
}
