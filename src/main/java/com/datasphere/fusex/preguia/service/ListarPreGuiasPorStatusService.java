package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.services.BuscarBenificarioPorPrecCPService;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.StatusGuia;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.preguia.dto.PreGuiaResumoResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ListarPreGuiasPorStatusService {

    private static final Set<StatusGuia> STATUS_PRE_GUIA = EnumSet.of(
            StatusGuia.PRE_GUIA, StatusGuia.COM_PENDENCIAS, StatusGuia.VALIDADA);

    private final GuiaRepository guiaRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final BuscarBenificarioPorPrecCPService buscarBenificarioPorPrecCPService;

    public ListarPreGuiasPorStatusService(GuiaRepository guiaRepository,
                                          GuiaProcRepository guiaProcRepository,
                                          BuscarBenificarioPorPrecCPService buscarBenificarioPorPrecCPService) {
        this.guiaRepository = guiaRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.buscarBenificarioPorPrecCPService = buscarBenificarioPorPrecCPService;
    }

    // Retorna null quando o status informado é inválido (mesmo padrão dos demais services do projeto)
    public List<PreGuiaResumoResponse> listarPreGuiasPorStatus(String status) {
        Set<String> filtro = STATUS_PRE_GUIA.stream().map(StatusGuia::name).collect(Collectors.toSet());

        if (status != null && !status.isBlank()) {
            StatusGuia statusInformado;
            try {
                statusInformado = StatusGuia.valueOf(status);
            } catch (IllegalArgumentException e) {
                return null;
            }

            if (!STATUS_PRE_GUIA.contains(statusInformado)) {
                return null;
            }

            filtro = Set.of(statusInformado.name());
        }

        List<GuiaModel> guias = guiaRepository.findByStatusInOrderByIdAsc(filtro);
        if (guias.isEmpty()) {
            return List.of();
        }

        List<Long> guiaIds = guias.stream().map(GuiaModel::getId).toList();
        Map<Long, List<GuiaProcModel>> procedimentosPorGuia = guiaProcRepository.findByGuiaModel_IdIn(guiaIds)
                .stream()
                .collect(Collectors.groupingBy(procedimento -> procedimento.getGuiaModel().getId()));

        return guias.stream()
                .map(guia -> montarResumo(guia, procedimentosPorGuia.getOrDefault(guia.getId(), List.of())))
                .toList();
    }

    private PreGuiaResumoResponse montarResumo(GuiaModel guia, List<GuiaProcModel> procedimentos) {
        BeneficiarioModel beneficiario = buscarBenificarioPorPrecCPService.buscarBeneficioarioPorPrecCP(guia.getBenfId());
        String beneficiarioNome = beneficiario != null ? beneficiario.getNome() : null;

        BigDecimal valorTotal = procedimentos.stream()
                .map(p -> p.getContratoModel().getValor().multiply(BigDecimal.valueOf(p.getQtdAutorizada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PreGuiaResumoResponse(
                guia.getId(),
                beneficiarioNome,
                StatusGuia.valueOf(guia.getStatus()),
                procedimentos.size(),
                valorTotal,
                guia.getData());
    }
}
