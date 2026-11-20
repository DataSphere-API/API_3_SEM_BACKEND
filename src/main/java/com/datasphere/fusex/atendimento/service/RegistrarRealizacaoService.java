package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.atendimento.SessaoModel;
import com.datasphere.fusex.atendimento.SessaoRepository;
import com.datasphere.fusex.atendimento.dto.BaixaResponse;
import com.datasphere.fusex.atendimento.dto.ValidarQrCodeRequest;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.atendimento.service.ValidacaoQrService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RegistrarRealizacaoService {

    private final GuiaProcRepository guiaProcRepository;
    private final ValidacaoQrService validacaoQrService;
    private final AtualizarStatusGuiaProcRealizadaService atualizarStatusGuiaProcRealizada;
    private final RecalcularStatusDaGuiaService recalcularStatusDaGuia;
    private final BuscarOcsAutenticadaService buscarOcsAutenticadaService;
    private final SessaoRepository sessaoRepository;

    public RegistrarRealizacaoService(GuiaProcRepository guiaProcRepository,
                                      ValidacaoQrService validacaoQrService,
                                      AtualizarStatusGuiaProcRealizadaService atualizarStatusGuiaProcRealizada,
                                      RecalcularStatusDaGuiaService recalcularStatusDaGuia,
                                      BuscarOcsAutenticadaService buscarOcsAutenticadaService,
                                      SessaoRepository sessaoRepository) {
        this.guiaProcRepository = guiaProcRepository;
        this.validacaoQrService = validacaoQrService;
        this.atualizarStatusGuiaProcRealizada = atualizarStatusGuiaProcRealizada;
        this.recalcularStatusDaGuia = recalcularStatusDaGuia;
        this.buscarOcsAutenticadaService = buscarOcsAutenticadaService;
        this.sessaoRepository = sessaoRepository;
    }

    @Transactional
    public BaixaResponse registrarRealizacao (ValidarQrCodeRequest request) {
        String cnpj = buscarOcsAutenticadaService.buscarOcsAutenticada();
        GuiaProcModel procedimento = buscarComBloqueio(request.qr());

        // validações do time (#68): se alguma falhar, nada foi gravado
        validacaoQrService.validarQrCodeDaOcs(procedimento, cnpj);
        validacaoQrService.validarGuiaDentroDaValidade(procedimento);
        validacaoQrService.validarProcedimentoNaoRealizado(procedimento);

        int qtdAutorizada = procedimento.getQtdAutorizada();
        int qtdRealizada = procedimento.getQtdRealizada();

        // caso especial: com 1 sessão autorizada, esta própria baixa é a primeira (e única) sessão
        if (qtdAutorizada == 1 && qtdRealizada == 0) {
            SessaoModel sessao = new SessaoModel();
            sessao.setGuiaProc(procedimento);
            sessao.setNumeroSessao(1);
            sessao.setDataRealizacao(LocalDateTime.now());
            sessao.setOcsId(cnpj);
            sessaoRepository.save(sessao);
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
                atualizado.getId(),
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
