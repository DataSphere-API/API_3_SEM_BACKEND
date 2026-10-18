package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.guia.dto.request.ValidacaoPreGuiaResponse;
import com.datasphere.fusex.guia.dto.request.ValidarPreGuiaResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

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

    record HistoricoStatus(Long preGuiaId, String statusAnterior, String statusNovo, LocalDateTime data) {}

    private static final Set<String> STATUS_VALIDAVEIS = Set.of("PRE_GUIA", "COM_PENDENCIAS");

    private final PreGuiaValidacaoService preGuiaValidacaoService;

    // TODO(US000-11): MOCKS TEMPORÁRIOS. Trocar por leitura/gravação real da Guia e do histórico.
    // Ids do mock: 7 = PRE_GUIA, 8 = COM_PENDENCIAS (valida limpo), 9 = APROVADA. Outros = 404.
    private final Map<Long, String> statusPorId = new ConcurrentHashMap<>(
            Map.of(7L, "PRE_GUIA", 8L, "COM_PENDENCIAS", 9L, "APROVADA"));
    private final List<HistoricoStatus> historico = new CopyOnWriteArrayList<>();

    public ValidarPreGuiaService(PreGuiaValidacaoService preGuiaValidacaoService) {
        this.preGuiaValidacaoService = preGuiaValidacaoService;
    }

    public ValidarPreGuiaResponse validarPreGuia(Long id) {
        String statusAtual = statusPorId.get(id);
        if (statusAtual == null) {
            throw new PreGuiaNaoEncontradaException(id);
        }
        if (!STATUS_VALIDAVEIS.contains(statusAtual)) {
            throw new PreGuiaStatusInvalidoException(id, statusAtual);
        }

        ValidacaoPreGuiaResponse validacao = preGuiaValidacaoService.consolidarPendencias(id);
        String novoStatus = validacao.validada() ? "VALIDADA" : "COM_PENDENCIAS";

        statusPorId.put(id, novoStatus);
        historico.add(new HistoricoStatus(id, statusAtual, novoStatus, LocalDateTime.now()));

        return new ValidarPreGuiaResponse(id, novoStatus, validacao.validada(), validacao.pendencias());
    }
}