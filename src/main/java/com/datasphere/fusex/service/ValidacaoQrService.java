package com.datasphere.fusex.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.dto.request.ConferenciaQrResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

    /** Erro: GUIA_EXPIRADA. Sem data de validade = sem prazo. */
    public void validarGuiaDentroDaValidade(GuiaProcModel p) {
        LocalDate validade = p.getDataValidade();
        if (validade != null && LocalDate.now().isAfter(validade)) {
            throw new BusinessException(ErrorCode.GUIA_EXPIRADA);
        }
    }

    /** Erro: PROCEDIMENTO_JA_REALIZADO */
    public void validarProcedimentoNaoRealizado(GuiaProcModel p) {
        if (STATUS_REALIZADO.equalsIgnoreCase(p.getStatus())) {
            throw new BusinessException(ErrorCode.PROCEDIMENTO_JA_REALIZADO);
        }
    }

    /**
     * US004-2: monta os dados para conferência do QR code.
     * Somente leitura: aplica validações de QR, OCS e validade, mas NÃO altera status.
     */
    @Transactional(readOnly = true)
    public ConferenciaQrResponse montarDadosConferenciaQr(String codigo, String cnpj) {
        GuiaProcModel p = buscarProcedimentoPorQrCode(codigo);
        validarQrCodeDaOcs(p, cnpj);
        validarGuiaDentroDaValidade(p);

        ContratoModel contrato = p.getContratoModel();
        GuiaModel guia = p.getGuiaModel();
        BeneficiarioModel beneficiario = guia.getBeneficiarioModel();

        int qtdAutorizada = p.getQtdAutorizada() == null ? 0 : p.getQtdAutorizada();
        int qtdRealizada = p.getQtdRealizada() == null ? 0 : p.getQtdRealizada();
        boolean prontoParaBaixa = qtdAutorizada > 0 && qtdRealizada >= qtdAutorizada;

        return new ConferenciaQrResponse(
                p.getGuiaId(),
                guia.getId(),
                new ConferenciaQrResponse.Beneficiario(beneficiario.getPrecCP(), beneficiario.getNome()),
                contrato.getProcedimentoModel().getNome(),
                new ConferenciaQrResponse.Ocs(contrato.getOcsModel().getCnpj(), contrato.getOcsModel().getNome()),
                qtdAutorizada,
                qtdRealizada,
                prontoParaBaixa,
                p.getDataValidade(),
                p.getStatus()
        );
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}