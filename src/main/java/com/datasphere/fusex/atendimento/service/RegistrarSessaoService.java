package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.atendimento.sessao.SessaoModel;
import com.datasphere.fusex.atendimento.sessao.SessaoRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.dto.request.SessaoRequest;
import com.datasphere.fusex.dto.request.SessaoResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.guiaProc.StatusProcedimento;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.service.ValidacaoQrService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegistrarSessaoService {

    private static final String STATUS_EM_ANDAMENTO = StatusProcedimento.EM_ANDAMENTO.name();

    private final GuiaProcRepository guiaProcRepository;
    private final SessaoRepository sessaoRepository;
    private final ValidarSessaoService validarSessaoService;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;
    private final BuscarOcsAutenticadaService buscarOcsAutenticadaService;
    private final ValidacaoQrService validacaoQrService;

    public RegistrarSessaoService(GuiaProcRepository guiaProcRepository,
                                  SessaoRepository sessaoRepository,
                                  ValidarSessaoService validarSessaoService,
                                  RegistrarMudancaStatusService registrarMudancaStatusService,
                                  BuscarOcsAutenticadaService buscarOcsAutenticadaService,
                                  ValidacaoQrService validacaoQrService) {
        this.guiaProcRepository = guiaProcRepository;
        this.sessaoRepository = sessaoRepository;
        this.validarSessaoService = validarSessaoService;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
        this.buscarOcsAutenticadaService = buscarOcsAutenticadaService;
        this.validacaoQrService = validacaoQrService;
    }

    @Transactional
    public SessaoResponse registrarSessao(Long atendimentoId, SessaoRequest request) {

        String cnpj = buscarOcsAutenticadaService.buscarOcsAutenticada();
        GuiaProcModel atendimento = guiaProcRepository.findByIdParaSessao(atendimentoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO));

        validarSessaoService.validarOcsDoContrato(atendimento, cnpj);
        validacaoQrService.validarProcedimentoNaoRealizado(atendimento);
        validacaoQrService.validarGuiaDentroDaValidade(atendimento);
        validarSessaoService.validarGuiaEmitida(atendimento);
        validarSessaoService.validarGuiaDentroDaValidade(atendimento);
        validarSessaoService.validarSaldoDeSessoes(atendimento);

        int proximoNumero = sessaoRepository
                .findMaxNumeroSessaoByGuiaProcId(atendimentoId)
                .map(max -> max + 1)
                .orElse(1);

        boolean primeiraSessao = !sessaoRepository.existsByGuiaProc_Id(atendimentoId);

        if (primeiraSessao) {
            String statusAnterior = atendimento.getStatus();
            atendimento.setStatus(STATUS_EM_ANDAMENTO);
            if (!STATUS_EM_ANDAMENTO.equalsIgnoreCase(statusAnterior)) {
                registrarMudancaStatusService.executar(EntidadeTipo.GUIA_PROC, atendimentoId,
                        statusAnterior, STATUS_EM_ANDAMENTO, "registro-sessao");
            }
        }

        LocalDateTime agora = LocalDateTime.now();

        SessaoModel sessao = new SessaoModel();
        sessao.setGuiaProc(atendimento);
        sessao.setNumeroSessao(proximoNumero);
        sessao.setDataRealizacao(agora);
        sessao.setOcsId(cnpj);
        sessaoRepository.save(sessao);

        int novaQtdRealizada = atendimento.getQtdRealizada() + 1;
        atendimento.setQtdRealizada(novaQtdRealizada);
        guiaProcRepository.save(atendimento);

        int saldo = atendimento.getQtdAutorizada() - novaQtdRealizada;

        return new SessaoResponse(
                atendimento.getId(),
                proximoNumero,
                atendimento.getQtdAutorizada(),
                novaQtdRealizada,
                saldo,
                atendimento.getStatus(),
                agora
        );
    }
}
