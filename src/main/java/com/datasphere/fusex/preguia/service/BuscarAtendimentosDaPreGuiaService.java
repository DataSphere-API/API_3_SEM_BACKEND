package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BuscarAtendimentosDaPreGuiaService {

    private final GuiaProcRepository guiaProcRepository;

    public BuscarAtendimentosDaPreGuiaService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    // id do procedimento da pré-guia -> id do atendimento (guia_proc) criado na emissão.
    // Antes da emissão não há atendimento e o mapa fica vazio.
    public Map<Long, Long> buscar(List<PreGuiaProcModel> procedimentos) {
        Map<Long, Long> atendimentoPorProcedimento = new HashMap<>();
        if (procedimentos.isEmpty() || procedimentos.get(0).getPreGuiaModel().getGuiaId() == null) {
            return atendimentoPorProcedimento;
        }
        List<GuiaProcModel> atendimentos =
                guiaProcRepository.findByGuiaModel_Id(procedimentos.get(0).getPreGuiaModel().getGuiaId());
        for (PreGuiaProcModel procedimento : procedimentos) {
            for (GuiaProcModel atendimento : atendimentos) {
                // Na mesma pré-guia o procedimento não repete na mesma OCS, então o par identifica o atendimento.
                boolean mesmoProcedimento = Objects.equals(procedimento.getProcedimentoModel().getId(),
                        atendimento.getContratoModel().getProcedimentoModel().getId());
                boolean mesmaOcs = Objects.equals(procedimento.getOcsModel().getCnpj(),
                        atendimento.getContratoModel().getOcsModel().getCnpj());
                if (mesmoProcedimento && mesmaOcs) {
                    atendimentoPorProcedimento.put(procedimento.getId(), atendimento.getId());
                    break;
                }
            }
        }
        return atendimentoPorProcedimento;
    }
}
