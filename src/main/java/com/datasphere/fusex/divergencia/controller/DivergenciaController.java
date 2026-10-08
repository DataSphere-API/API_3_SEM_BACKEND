package com.datasphere.fusex.divergencia.controller;

import com.datasphere.fusex.divergencia.dto.DivergenciasResponse;
import com.datasphere.fusex.divergencia.service.ConsolidarDivergenciasService;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DivergenciaController {

    private final BuscarFaturaPorIdService buscarFaturaPorIdService;
    private final ConsolidarDivergenciasService consolidarDivergenciasService;

    public DivergenciaController(BuscarFaturaPorIdService buscarFaturaPorIdService,
                                 ConsolidarDivergenciasService consolidarDivergenciasService) {
        this.buscarFaturaPorIdService = buscarFaturaPorIdService;
        this.consolidarDivergenciasService = consolidarDivergenciasService;
    }

    @GetMapping("/faturas/{id}/divergencias")
    public ResponseEntity<?> consultarDivergencias(@PathVariable Long id) {
        FaturaModel fatura = buscarFaturaPorIdService.buscarFaturaPorId(id);

        if (fatura == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FATURA_NAO_ENCONTRADA");
        }

        DivergenciasResponse resposta = consolidarDivergenciasService.consolidarDivergencias(fatura);
        return ResponseEntity.ok(resposta);
    }
}