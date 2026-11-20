package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.ocs.OcsRepository;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.dto.ProcedimentoPreGuiaRequest;
import com.datasphere.fusex.procedimento.ProcedimentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalvarProcedimentosDaPreGuiaService {

    private final PreGuiaProcRepository preGuiaProcRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final OcsRepository ocsRepository;

    public SalvarProcedimentosDaPreGuiaService(PreGuiaProcRepository preGuiaProcRepository,
                                               ProcedimentoRepository procedimentoRepository,
                                               OcsRepository ocsRepository) {
        this.preGuiaProcRepository = preGuiaProcRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.ocsRepository = ocsRepository;
    }

    // Executado dentro da transação do cadastro/edição, depois da validação completa.
    public void salvar(PreGuiaModel preGuia, List<ProcedimentoPreGuiaRequest> pedidos) {
        var procedimentos = pedidos.stream().map(item -> {
            var procedimento = new PreGuiaProcModel();
            procedimento.setPreGuiaModel(preGuia);
            procedimento.setProcedimentoModel(procedimentoRepository.getReferenceById(item.procedimentoId()));
            procedimento.setOcsModel(ocsRepository.getReferenceById(item.ocsCnpj()));
            procedimento.setQtdAutorizada(item.quantidade());
            return procedimento;
        }).toList();
        preGuiaProcRepository.saveAll(procedimentos);
    }
}
