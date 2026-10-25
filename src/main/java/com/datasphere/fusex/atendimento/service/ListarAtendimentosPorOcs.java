package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
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

    GuiaProcRepository guiaProcRepository;

    public ListarAtendimentosPorOcs(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    @Transactional(readOnly = true)
    public List<AtendimentoResponse> listarAtendimentosPorOcs(String cnpj) {
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
                atendimento.getGuiaId(),
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