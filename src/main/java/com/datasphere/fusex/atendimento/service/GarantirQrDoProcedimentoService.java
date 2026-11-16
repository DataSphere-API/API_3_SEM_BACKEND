package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.StatusGuia;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class GarantirQrDoProcedimentoService {

    private final GuiaProcRepository guiaProcRepository;
    private final GerarCodigoUnicoQrService gerarCodigoUnicoQrService;
    private final SalvarQrNoProcedimentoService salvarQrNoProcedimentoService;
    private final ValidarProcedimentoNaoRealizadoService validarProcedimentoNaoRealizadoService;
    private final ValidadeGuiaService validadeGuiaService;

    public GarantirQrDoProcedimentoService(GuiaProcRepository guiaProcRepository,
                                           GerarCodigoUnicoQrService gerarCodigoUnicoQrService,
                                           SalvarQrNoProcedimentoService salvarQrNoProcedimentoService,
                                           ValidarProcedimentoNaoRealizadoService validarProcedimentoNaoRealizadoService,
                                           ValidadeGuiaService validadeGuiaService) {
        this.guiaProcRepository = guiaProcRepository;
        this.gerarCodigoUnicoQrService = gerarCodigoUnicoQrService;
        this.salvarQrNoProcedimentoService = salvarQrNoProcedimentoService;
        this.validarProcedimentoNaoRealizadoService = validarProcedimentoNaoRealizadoService;
        this.validadeGuiaService = validadeGuiaService;
    }

    @Transactional
    public String garantirQrDoProcedimento(Long atendimentoId) {
        if (atendimentoId == null) {
            throw new BusinessException(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO);
        }
        GuiaProcModel procedimento = guiaProcRepository.findByIdParaQr(atendimentoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO));
        validarProcedimentoNaoRealizadoService.validarProcedimentoNaoRealizado(procedimento);
        validarGuia(procedimento);

        if (procedimento.getQr() != null && !procedimento.getQr().isBlank()) {
            return procedimento.getQr();
        }
        String codigo = gerarCodigoUnicoQrService.gerarCodigoUnicoQr();
        salvarQrNoProcedimentoService.salvarQrNoProcedimento(procedimento, codigo);
        return codigo;
    }

    private void validarGuia(GuiaProcModel procedimento) {
        GuiaModel guia = procedimento.getGuiaModel();
        if (guia == null) {
            throw new BusinessException(ErrorCode.GUIA_NAO_EMITIDA);
        }
        if (StatusGuia.EXPIRADA.name().equalsIgnoreCase(guia.getStatus())) {
            throw new BusinessException(ErrorCode.GUIA_EXPIRADA);
        }
        if (!StatusGuia.EMITIDA.name().equalsIgnoreCase(guia.getStatus())
                && !StatusGuia.PARCIALMENTE_REALIZADA.name().equalsIgnoreCase(guia.getStatus())) {
            throw new BusinessException(ErrorCode.GUIA_NAO_EMITIDA);
        }
        LocalDate hoje = LocalDate.now();
        LocalDate emissao = guia.getDataEmissao();
        if (emissao == null || hoje.isBefore(emissao)
                || hoje.isAfter(validadeGuiaService.calcularDataValidade(emissao))
                || (procedimento.getDataValidade() != null && hoje.isAfter(procedimento.getDataValidade()))) {
            throw new BusinessException(ErrorCode.GUIA_EXPIRADA);
        }
    }
}
