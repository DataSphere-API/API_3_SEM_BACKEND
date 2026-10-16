package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.dto.request.BaixaResponse;
import com.datasphere.fusex.dto.request.ValidarQrCodeRequest;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.service.ValidacaoQrService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RegistrarRealizacaoService {

    private final GuiaProcRepository guiaProcRepository;
    private final ValidacaoQrService validacaoQrService;
    private final AtualizarStatusGuiaProcRealizadaService atualizarStatusGuiaProcRealizada;
    private final RecalcularStatusDaGuiaService recalcularStatusDaGuia;

    public RegistrarRealizacaoService(GuiaProcRepository guiaProcRepository,
                                      ValidacaoQrService validacaoQrService,
                                      AtualizarStatusGuiaProcRealizadaService atualizarStatusGuiaProcRealizada,
                                      RecalcularStatusDaGuiaService recalcularStatusDaGuia) {
        this.guiaProcRepository = guiaProcRepository;
        this.validacaoQrService = validacaoQrService;
        this.atualizarStatusGuiaProcRealizada = atualizarStatusGuiaProcRealizada;
        this.recalcularStatusDaGuia = recalcularStatusDaGuia;
    }

    @Transactional
    public BaixaResponse registrarRealizacao (ValidarQrCodeRequest request) {
        GuiaProcModel procedimento = buscarComBloqueio(request.qr());

        // validações do time (#68): se alguma falhar, nada foi gravado
        validacaoQrService.validarQrCodeDaOcs(procedimento, request.cnpj());
        validacaoQrService.validarGuiaDentroDaValidade(procedimento);
        validacaoQrService.validarProcedimentoNaoRealizado(procedimento);

        int qtdAutorizada = procedimento.getQtdAutorizada();
        int qtdRealizada = procedimento.getQtdRealizada();

        // caso especial: com 1 sessão autorizada, esta própria baixa é a primeira (e única) sessão
        if (qtdAutorizada == 1 && qtdRealizada == 0) {
            qtdRealizada = 1;
            procedimento.setQtdRealizada(qtdRealizada);
        }

        if (qtdRealizada < qtdAutorizada) {
            Map<String, Object> detalhes = new LinkedHashMap<>();
            detalhes.put("qtdRealizada", qtdRealizada);
            detalhes.put("qtdAutorizada", qtdAutorizada);
            detalhes.put("saldo", qtdAutorizada - qtdRealizada);
            throw new BusinessException(ErrorCode.SESSOES_INCOMPLETAS, detalhes);
        }

        GuiaProcModel atualizado = atualizarStatusGuiaProcRealizada.atualizarStatusGuiaProcRealizada(procedimento);

        Long guiaId = atualizado.getGuiaModel() != null ? atualizado.getGuiaModel().getId() : null;
        String statusGuia = guiaId != null ? recalcularStatusDaGuia.recalcularStatusDaGuia(guiaId) : null;

        return new BaixaResponse(
                atualizado.getGuiaId(),
                atualizado.getStatus(),
                qtdAutorizada,
                qtdRealizada,
                atualizado.getDataValidacao(),
                statusGuia
        );
    }

    private GuiaProcModel buscarComBloqueio(String qr) {
        if (qr == null || qr.isBlank()) {
            throw new BusinessException(ErrorCode.QR_NAO_ENCONTRADO);
        }
        return guiaProcRepository.findByQrParaBaixa(qr.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_NAO_ENCONTRADO));
    }
}