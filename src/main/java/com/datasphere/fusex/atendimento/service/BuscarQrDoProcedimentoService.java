package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.dto.request.QrCodeResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.QrCodeIndisponivelException;
import com.datasphere.fusex.guia.StatusGuia;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.guiaProc.StatusProcedimento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDate;

@Service
public class BuscarQrDoProcedimentoService {

    private final GuiaProcRepository guiaProcRepository;
    private final ValidarDonoDaGuiaService validarDonoDaGuiaService;
    private final ValidadeGuiaService validadeGuiaService;

    public BuscarQrDoProcedimentoService(GuiaProcRepository guiaProcRepository,
                                         ValidarDonoDaGuiaService validarDonoDaGuiaService,
                                         ValidadeGuiaService validadeGuiaService) {
        this.guiaProcRepository = guiaProcRepository;
        this.validarDonoDaGuiaService = validarDonoDaGuiaService;
        this.validadeGuiaService = validadeGuiaService;
    }

    @Transactional(readOnly = true)
    public QrCodeResponse buscarQrDoProcedimento(Long atendimentoId, Principal principal) {
        String precCp = validarDonoDaGuiaService.identificarBeneficiario(principal);
        if (atendimentoId == null) {
            throw new BusinessException(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO);
        }
        GuiaProcModel procedimento = guiaProcRepository.findById(atendimentoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO));
        validarDonoDaGuiaService.validarDonoDaGuia(procedimento.getGuiaModel(), precCp);
        validarDisponibilidade(procedimento);
        if (procedimento.getQr() == null || procedimento.getQr().isBlank()) {
            throw new QrCodeIndisponivelException(ErrorCode.QR_NAO_GERADO);
        }
        return new QrCodeResponse(procedimento.getQr());
    }

    private void validarDisponibilidade(GuiaProcModel procedimento) {
        var guia = procedimento.getGuiaModel();
        LocalDate hoje = LocalDate.now();
        String status = procedimento.getStatus();
        if (StatusGuia.EXPIRADA.name().equalsIgnoreCase(guia.getStatus())
                || StatusProcedimento.EXPIRADA.name().equalsIgnoreCase(status)
                || "EXPIRADO".equalsIgnoreCase(status)
                || guia.getDataEmissao() == null || hoje.isBefore(guia.getDataEmissao())
                || hoje.isAfter(validadeGuiaService.calcularDataValidade(guia.getDataEmissao()))
                || (procedimento.getDataValidade() != null && hoje.isAfter(procedimento.getDataValidade()))) {
            throw new QrCodeIndisponivelException(ErrorCode.GUIA_EXPIRADA);
        }
        if (StatusProcedimento.REALIZADA.name().equalsIgnoreCase(status)
                || StatusProcedimento.FATURADA.name().equalsIgnoreCase(status)
                || "REALIZADO".equalsIgnoreCase(status)) {
            throw new QrCodeIndisponivelException(ErrorCode.PROCEDIMENTO_JA_REALIZADO);
        }
        if ((!StatusGuia.EMITIDA.name().equalsIgnoreCase(guia.getStatus())
                && !StatusGuia.PARCIALMENTE_REALIZADA.name().equalsIgnoreCase(guia.getStatus()))
                || (!StatusProcedimento.PENDENTE.name().equalsIgnoreCase(status)
                && !StatusProcedimento.EM_ANDAMENTO.name().equalsIgnoreCase(status))) {
            throw new QrCodeIndisponivelException(ErrorCode.GUIA_NAO_EMITIDA);
        }
        Integer autorizada = procedimento.getQtdAutorizada();
        Integer realizada = procedimento.getQtdRealizada();
        if (autorizada == null || autorizada <= 0 || realizada == null || realizada < autorizada) {
            throw new QrCodeIndisponivelException(ErrorCode.SESSOES_INCOMPLETAS);
        }
    }
}
