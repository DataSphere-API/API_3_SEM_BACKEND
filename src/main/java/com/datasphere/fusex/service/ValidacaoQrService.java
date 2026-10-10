package com.datasphere.fusex.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidacaoQrService {

    private static final String STATUS_REALIZADO = "REALIZADO"; // AJUSTE: valor real usado em GuiaProcModel.status

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
        GuiaProcModel procedimento = guiaProcRepository.findByQr(qr.trim());
        if (procedimento == null) {
            throw new BusinessException(ErrorCode.QR_NAO_ENCONTRADO);
        }
        return procedimento;
    }

    /** A OCS que leu o QR precisa ser a do contrato. Erro: OCS_DIFERENTE_DO_CONTRATO */
    public void validarQrCodeDaOcs(GuiaProcModel p, String cnpj) {
        ContratoModel contrato = p.getContratoModel();
        if (contrato == null || contrato.getOcsModel() == null) {
            throw new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
        }

        String cnpjContrato = somenteDigitos(contrato.getOcsModel().getCnpj());
        String cnpjInformado = somenteDigitos(cnpj);

        if (cnpjInformado.isEmpty() || !cnpjInformado.equals(cnpjContrato)) {
            throw new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
        }
    }

    /** Erro: PROCEDIMENTO_JA_REALIZADO */
    public void validarProcedimentoNaoRealizado(GuiaProcModel p) {
        if (STATUS_REALIZADO.equalsIgnoreCase(p.getStatus())) {
            throw new BusinessException(ErrorCode.PROCEDIMENTO_JA_REALIZADO);
        }
    }

    // PENDENTES (faltam campos nas entidades, ver Passo 7):
    // - validarGuiaDentroDaValidade(GuiaProcModel p)  -> GUIA_EXPIRADA
    // - validarSessoesCompletas(GuiaProcModel p)      -> SESSOES_INCOMPLETAS
    // - montarResultado(GuiaProcModel p)              -> ValidacaoQrResultado

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}