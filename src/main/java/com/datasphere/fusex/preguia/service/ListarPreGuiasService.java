package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.dto.PreGuiaResumoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarPreGuiasService {

    // Injeção de dependência das classes que estavam dando erro
    private final PreGuiaProcRepository preGuiaProcRepository;
    private final CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService;
    //private final BuscarDataEmissaoDaPreGuiaService buscarDataEmissaoDaPreGuiaService;

    private PreGuiaResumoResponse montarResumo(PreGuiaModel preGuia) {
        // O erro do 'List' foi resolvido adicionando o import acima
        List<PreGuiaProcModel> procedimentos = preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(preGuia.getId());

                return new PreGuiaResumoResponse(
                preGuia.getId(),
                preGuia.getBeneficiarioModel().getNome(),
                preGuia.getStatus(),
                procedimentos.size(),
                calcularValorTotalPreGuiaService.calcularValorTotal(procedimentos),
                preGuia.getDataCriacao(),
                preGuia.getDataAprovacaoMedico() != null,
                preGuia.getDataAprovacaoChefe() != null,
                null, // <--- Trocamos a chamada do serviço que não existe por null
                preGuia.getGuiaId()
        );
    }
}