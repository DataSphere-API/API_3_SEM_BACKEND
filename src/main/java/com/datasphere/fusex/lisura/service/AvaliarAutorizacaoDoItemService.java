package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.contrato.service.CalcularValorContratadoService;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.fatura.service.CalcularDivergenciaValorService;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class AvaliarAutorizacaoDoItemService {

    private final CalcularDivergenciaValorService calcularDivergenciaValorService;
    private final CalcularValorContratadoService calcularValorContratadoService;

    public AvaliarAutorizacaoDoItemService(CalcularDivergenciaValorService calcularDivergenciaValorService,
                                           CalcularValorContratadoService calcularValorContratadoService) {
        this.calcularDivergenciaValorService = calcularDivergenciaValorService;
        this.calcularValorContratadoService = calcularValorContratadoService;
    }

    // O contratado do item é o contrato por sessão vezes as sessões previstas na guia.
    public LisuraItemResponse avaliarAutorizacaoDoItem(EspelhoItemModel item, GuiaProcModel atendimento,
                                                       String ocsId, LocalDate dataAtendimento) {
        ContratoModel contrato = atendimento == null ? null : atendimento.getContratoModel();
        if (contrato == null) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    null, false, "Procedimento sem contrato");
        }
        if (!contrato.getOcsModel().getCnpj().equals(ocsId)) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    null, false, "Procedimento sem contrato com a OCS da fatura");
        }
        BigDecimal valorContratado = calcularValorContratadoService.calcular(atendimento);
        LocalDate dataValidade = atendimento.getDataValidade();
        if (dataValidade != null && dataAtendimento == null) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    valorContratado, false, "Atendimento sem data registrada");
        }
        if (dataValidade != null && dataAtendimento.isAfter(dataValidade)) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    valorContratado, false, "Guia vencida: atendimento após o prazo de validade");
        }
        if (calcularDivergenciaValorService.calcularDivergenciaValor(item.getValor(), valorContratado).signum() > 0) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    valorContratado, false, "Valor apresentado acima do valor contratado");
        }
        return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                valorContratado, true, null);
    }
}
