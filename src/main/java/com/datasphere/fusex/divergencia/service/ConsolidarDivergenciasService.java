package com.datasphere.fusex.divergencia.service;

import com.datasphere.fusex.divergencia.dto.DivergenciaItemResponse;
import com.datasphere.fusex.divergencia.dto.DivergenciasResponse;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.service.CalcularDivergenciaValorService;
import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import com.datasphere.fusex.lisura.dto.LisuraResponse;
import com.datasphere.fusex.lisura.service.GerarLisuraDaFaturaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsolidarDivergenciasService {

    private final GerarLisuraDaFaturaService gerarLisuraDaFaturaService;
    private final CalcularDivergenciaValorService calcularDivergenciaValorService;
    private final ClassificarSeveridadeDivergenciaService classificarSeveridadeDivergenciaService;

    public ConsolidarDivergenciasService(GerarLisuraDaFaturaService gerarLisuraDaFaturaService,
                                         CalcularDivergenciaValorService calcularDivergenciaValorService,
                                         ClassificarSeveridadeDivergenciaService classificarSeveridadeDivergenciaService) {
        this.gerarLisuraDaFaturaService = gerarLisuraDaFaturaService;
        this.calcularDivergenciaValorService = calcularDivergenciaValorService;
        this.classificarSeveridadeDivergenciaService = classificarSeveridadeDivergenciaService;
    }

    public DivergenciasResponse consolidarDivergencias(FaturaModel fatura) {
        LisuraResponse lisura = gerarLisuraDaFaturaService.gerarLisuraDaFatura(fatura);

        List<DivergenciaItemResponse> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (LisuraItemResponse itemDaLisura : lisura.itens()) {
            BigDecimal contratado = itemDaLisura.valorContratado();

            if (contratado == null) {
                contratado = BigDecimal.ZERO;
            }

            BigDecimal divergencia = calcularDivergenciaValorService
                    .calcularDivergenciaValor(itemDaLisura.valorApresentado(), contratado);

            itens.add(new DivergenciaItemResponse(
                    itemDaLisura.espelhoItemId(),
                    divergencia,
                    itemDaLisura.autorizado(),
                    classificarSeveridadeDivergenciaService.classificarSeveridadeDivergencia(divergencia, contratado)
            ));

            total = total.add(divergencia);
        }

        return new DivergenciasResponse(lisura.faturaId(), itens, total);
    }
}