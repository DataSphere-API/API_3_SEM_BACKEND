package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.atendimento.dto.AtendimentoResponse;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ListarAtendimentosPorOcs {

    private final GuiaProcRepository guiaProcRepository;
    private final BuscarOcsAutenticadaService buscarOcsAutenticadaService;
    private final EspelhoItemRepository espelhoItemRepository;
    private final EspelhoRepository espelhoRepository;

    public ListarAtendimentosPorOcs(GuiaProcRepository guiaProcRepository,
                                    BuscarOcsAutenticadaService buscarOcsAutenticadaService,
                                    EspelhoItemRepository espelhoItemRepository,
                                    EspelhoRepository espelhoRepository) {
        this.guiaProcRepository = guiaProcRepository;
        this.buscarOcsAutenticadaService = buscarOcsAutenticadaService;
        this.espelhoItemRepository = espelhoItemRepository;
        this.espelhoRepository = espelhoRepository;
    }

    // A OCS só lista os atendimentos dela.
    @Transactional(readOnly = true)
    public List<AtendimentoResponse> listarAtendimentosPorOcs(String cnpj) {
        String ocsLogada = buscarOcsAutenticadaService.buscarOcsAutenticada();
        if (!ocsLogada.equals(cnpj)) {
            throw new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
        }
        List<GuiaProcModel> atendimentos = guiaProcRepository.findByContratoModel_OcsModel_Cnpj(cnpj);
        Map<Long, Integer> espelhoPorAtendimento = buscarEspelhos(atendimentos);
        List<AtendimentoResponse> respostas = new ArrayList<>();
        for (GuiaProcModel atendimento : atendimentos) {
            respostas.add(montarResposta(atendimento, espelhoPorAtendimento.get(atendimento.getId())));
        }
        return respostas;
    }

    // Em qual espelho cada atendimento já entrou; espelho cancelado não conta.
    private Map<Long, Integer> buscarEspelhos(List<GuiaProcModel> atendimentos) {
        Map<Long, Integer> espelhoPorAtendimento = new HashMap<>();
        if (atendimentos.isEmpty()) {
            return espelhoPorAtendimento;
        }
        List<Long> ids = atendimentos.stream().map(GuiaProcModel::getId).toList();
        for (EspelhoItemModel item : espelhoItemRepository.findByAtendimentoIdIn(ids)) {
            EspelhoModel espelho = espelhoRepository.findById(item.getEspelhoId()).orElse(null);
            if (espelho != null && espelho.getStatus() != StatusEspelho.CANCELADO) {
                espelhoPorAtendimento.put(item.getAtendimentoId(), espelho.getId());
            }
        }
        return espelhoPorAtendimento;
    }

    private AtendimentoResponse montarResposta(GuiaProcModel atendimento, Integer espelhoId) {
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
                atendimento.getDataValidacao(),
                espelhoId);
    }
}
