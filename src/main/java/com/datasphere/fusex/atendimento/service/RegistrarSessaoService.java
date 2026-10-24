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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegistrarSessaoService {

    private static final String STATUS_EM_ANDAMENTO = StatusProcedimento.EM_ANDAMENTO.name();

    private final GuiaProcRepository guiaProcRepository;
    private final SessaoRepository sessaoRepository;
    private final ValidarSessaoService validarSessaoService;

    public RegistrarSessaoService(GuiaProcRepository guiaProcRepository,
                                  SessaoRepository sessaoRepository,
                                  ValidarSessaoService validarSessaoService) {
        this.guiaProcRepository = guiaProcRepository;
        this.sessaoRepository = sessaoRepository;
        this.validarSessaoService = validarSessaoService;
    }

    @Transactional
    public SessaoResponse registrarSessao(Long atendimentoId, SessaoRequest request) {

        GuiaProcModel atendimento = guiaProcRepository.findById(atendimentoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO));

        validarSessaoService.validarOcsDoContrato(atendimento, request.cnpj());
        validarSessaoService.validarGuiaEmitida(atendimento);
        validarSessaoService.validarGuiaDentroDaValidade(atendimento);
        validarSessaoService.validarSaldoDeSessoes(atendimento);

        int proximoNumero = sessaoRepository
                .findMaxNumeroSessaoByGuiaProcId(atendimentoId)
                .map(max -> max + 1)
                .orElse(1);

        boolean primeiraSessao = !sessaoRepository.existsByGuiaProc_GuiaId(atendimentoId);

        if (primeiraSessao) {
            atendimento.setStatus(STATUS_EM_ANDAMENTO);
        }

        LocalDateTime agora = LocalDateTime.now();

        SessaoModel sessao = new SessaoModel();
        sessao.setGuiaProc(atendimento);
        sessao.setNumeroSessao(proximoNumero);
        sessao.setDataRealizacao(agora);
        sessao.setOcsId(request.cnpj().trim());
        sessaoRepository.save(sessao);

        int novaQtdRealizada = atendimento.getQtdRealizada() + 1;
        atendimento.setQtdRealizada(novaQtdRealizada);
        guiaProcRepository.save(atendimento);

        int saldo = atendimento.getQtdAutorizada() - novaQtdRealizada;

        return new SessaoResponse(
                atendimento.getGuiaId(),
                proximoNumero,
                atendimento.getQtdAutorizada(),
                novaQtdRealizada,
                saldo,
                atendimento.getStatus(),
                agora
        );
    }
}