package com.datasphere.fusex.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.repository.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidacaoQrService {

    private static final String STATUS_REALIZADO = "REALIZADO"; // AJUSTE: valor real usado no campo status

    private final GuiaProcRepository guiaProcRepository;

    public ValidacaoQrService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    /** Erro: QR_NAO_ENCONTRADO */
    @Transactional(readOnly = true)
    public GuiaProcModel buscarProcedimentoPorQrCode(String qr) {
        if (qr == null || qr.isBlank()) {
            throw new BusinessException(ErrorCode.QR_NAO_ENCONTRADO);
        }
        return guiaProcRepository.findByQr(qr.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_NAO_ENCONTRADO));
    }

    /** Erro: PROCEDIMENTO_JA_REALIZADO */
    public void validarProcedimentoNaoRealizado(GuiaProcModel p) {
        if (STATUS_REALIZADO.equalsIgnoreCase(p.getStatus())) {
            throw new BusinessException(ErrorCode.PROCEDIMENTO_JA_REALIZADO);
        }
    }

    // Faltam: validarQrCodeDaOcs, validarGuiaDentroDaValidade, validarSessoesCompletas
    // Dependem das entidades de contrato, guia, ocs e atendimento (ver "O que falta").
}