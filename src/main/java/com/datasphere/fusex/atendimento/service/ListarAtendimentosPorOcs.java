package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.dto.request.AtendimentoResponse;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListarAtendimentosPorOcs {

    private final GuiaProcRepository guiaProcRepository;
    private final BuscarOcsAutenticadaService buscarOcsAutenticadaService;

    public ListarAtendimentosPorOcs(GuiaProcRepository guiaProcRepository,
                                    BuscarOcsAutenticadaService buscarOcsAutenticadaService) {
        this.guiaProcRepository = guiaProcRepository;
        this.buscarOcsAutenticadaService = buscarOcsAutenticadaService;
    }

    // A OCS só lista os atendimentos dela.
    @Transactional(readOnly = true)
    public List<AtendimentoResponse> listarAtendimentosPorOcs(String cnpj) {
        String ocsLogada = buscarOcsAutenticadaService.buscarOcsAutenticada();
        if (!ocsLogada.equals(cnpj)) {
            throw new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
        }
        List<AtendimentoResponse> respostas = new ArrayList<>();
        for (GuiaProcModel atendimento : guiaProcRepository.findByContratoModel_OcsModel_Cnpj(cnpj)) {
            respostas.add(montarResposta(atendimento));
        }
        return respostas;
    }

    private AtendimentoResponse montarResposta(GuiaProcModel atendimento) {
        GuiaModel guia = atendimento.getGuiaModel();
        BeneficiarioModel beneficiario = guia.getBeneficiarioModel();

        return new AtendimentoResponse(
                atendimento.getId(),
                guia.getId(),
                new AtendimentoResponse.Beneficiario(beneficiario.getPrecCP(), beneficiario.getNome()),
                atendimento.getContratoModel().getProcedimentoModel().getNome(),
                new AtendimentoResponse.Ocs(
                        atendimento.getContratoModel().getOcsModel().getCnpj(),
                        atendimento.getContratoModel().getOcsModel().getNome()),
                atendimento.getQtdAutorizada(),
                atendimento.getQtdRealizada(),
                atendimento.getStatus(),
                atendimento.getDataValidade(),
                atendimento.getDataValidacao());
    }
}