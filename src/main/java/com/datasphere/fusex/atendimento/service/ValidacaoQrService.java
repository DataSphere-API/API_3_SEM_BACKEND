package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.atendimento.dto.ConferenciaQrResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.QrCodeIndisponivelException;
import com.datasphere.fusex.atendimento.service.BuscarOcsAutenticadaService;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

import java.time.LocalDate;

@Service
public class ValidacaoQrService {

    private static final Set<String> STATUS_JA_REALIZADO = Set.of("REALIZADO", "REALIZADA", "FATURADA");
    private final GuiaProcRepository guiaProcRepository;
    private final BuscarOcsAutenticadaService buscarOcsAutenticadaService;
    private final ValidadeGuiaService validadeGuiaService;

    public ValidacaoQrService(GuiaProcRepository guiaProcRepository,
                             BuscarOcsAutenticadaService buscarOcsAutenticadaService,
                             ValidadeGuiaService validadeGuiaService) {
        this.guiaProcRepository = guiaProcRepository;
        this.buscarOcsAutenticadaService = buscarOcsAutenticadaService;
        this.validadeGuiaService = validadeGuiaService;
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

    /** Considera tanto o vencimento do procedimento quanto o prazo da emissão da guia. */
    public void validarGuiaDentroDaValidade(GuiaProcModel p) {
        LocalDate validade = p.getDataValidade();
        GuiaModel guia = p.getGuiaModel();
        LocalDate emissao = guia == null ? null : guia.getDataEmissao();
        if (emissao != null && (LocalDate.now().isBefore(emissao)
                || LocalDate.now().isAfter(validadeGuiaService.calcularDataValidade(emissao)))) {
            throw new QrCodeIndisponivelException(ErrorCode.GUIA_EXPIRADA);
        }
        if ((validade != null && LocalDate.now().isAfter(validade))
                || (p.getGuiaModel() != null && "EXPIRADA".equalsIgnoreCase(p.getGuiaModel().getStatus()))) {
            throw new QrCodeIndisponivelException(ErrorCode.GUIA_EXPIRADA);
        }
    }

    /** Erro: PROCEDIMENTO_JA_REALIZADO */
    public void validarProcedimentoNaoRealizado(GuiaProcModel p) {
        if ("EXPIRADA".equalsIgnoreCase(p.getStatus()) || "EXPIRADO".equalsIgnoreCase(p.getStatus())) {
            throw new QrCodeIndisponivelException(ErrorCode.GUIA_EXPIRADA);
        }
        boolean jaRealizado = STATUS_JA_REALIZADO.stream()
                .anyMatch(s -> s.equalsIgnoreCase(p.getStatus()));
        if (jaRealizado) {
            throw new QrCodeIndisponivelException(ErrorCode.PROCEDIMENTO_JA_REALIZADO);
        }
    }

    /**
     * US004-2: monta os dados para conferência do QR code.
     * Somente leitura: aplica validações de QR, OCS e validade, mas NÃO altera status.
     */
    @Transactional(readOnly = true)
    public ConferenciaQrResponse montarDadosConferenciaQr(String codigo) {
        String cnpj = buscarOcsAutenticadaService.buscarOcsAutenticada();
        GuiaProcModel p = buscarProcedimentoPorQrCode(codigo);
        validarQrCodeDaOcs(p, cnpj);
        validarGuiaDentroDaValidade(p);
        validarProcedimentoNaoRealizado(p);

        ContratoModel contrato = p.getContratoModel();
        GuiaModel guia = p.getGuiaModel();
        BeneficiarioModel beneficiario = guia.getBeneficiarioModel();

        int qtdAutorizada = p.getQtdAutorizada() == null ? 0 : p.getQtdAutorizada();
        int qtdRealizada = p.getQtdRealizada() == null ? 0 : p.getQtdRealizada();
        boolean prontoParaBaixa = qtdAutorizada > 0
                && (qtdRealizada >= qtdAutorizada || (qtdAutorizada == 1 && qtdRealizada == 0));

        return new ConferenciaQrResponse(
                p.getId(),
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
