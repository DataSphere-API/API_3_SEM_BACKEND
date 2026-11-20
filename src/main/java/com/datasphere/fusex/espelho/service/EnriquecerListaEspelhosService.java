package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.EspelhoResumoResponse;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.ListarHistoricoPorEntidadeService;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.ocs.OcsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnriquecerListaEspelhosService {

    private final OcsRepository ocsRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService;

    public EnriquecerListaEspelhosService(OcsRepository ocsRepository,
                                          EspelhoItemRepository espelhoItemRepository,
                                          ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService) {
        this.ocsRepository = ocsRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.listarHistoricoPorEntidadeService = listarHistoricoPorEntidadeService;
    }

    public List<EspelhoResumoResponse> enriquecer(List<EspelhoModel> espelhos) {
        if (espelhos.isEmpty()) {
            return List.of();
        }
        Map<String, String> nomesPorCnpj = ocsRepository.findAllById(
                espelhos.stream()
                        .map(EspelhoModel::getOcsId)
                        .filter(ocsId -> ocsId != null && !ocsId.isBlank())
                        .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(OcsModel::getCnpj, OcsModel::getNome));

        List<EspelhoResumoResponse> resumos = new ArrayList<>();
        for (EspelhoModel espelho : espelhos) {
            List<EspelhoItemModel> itens = espelhoItemRepository.findByEspelhoId(espelho.getId());
            BigDecimal valorTotal = itens.stream()
                    .map(EspelhoItemModel::getValor)
                    .filter(valor -> valor != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            resumos.add(EspelhoResumoResponse.from(espelho, nomesPorCnpj.get(espelho.getOcsId()), itens.size(),
                    valorTotal, contarDevolucoes(espelho)));
        }
        return resumos;
    }

    // Quantas vezes o auditor devolveu o espelho para a OCS: sai do histórico, sem coluna nova.
    private int contarDevolucoes(EspelhoModel espelho) {
        return (int) listarHistoricoPorEntidadeService.executar(EntidadeTipo.ESPELHO, espelho.getId().longValue())
                .stream()
                .filter(evento -> StatusEspelho.REPROVADO.name().equals(evento.getStatusNovo()))
                .count();
    }
}
