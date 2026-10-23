package com.datasphere.fusex.lisura.controller;

import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import com.datasphere.fusex.lisura.dto.LisuraResponse;
import com.datasphere.fusex.lisura.service.GerarLisuraDaFaturaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LisuraController {

    private final BuscarFaturaPorIdService buscarFaturaPorIdService;
    private final GerarLisuraDaFaturaService gerarLisuraDaFaturaService;

    public LisuraController(BuscarFaturaPorIdService buscarFaturaPorIdService,
                            GerarLisuraDaFaturaService gerarLisuraDaFaturaService) {
        this.buscarFaturaPorIdService = buscarFaturaPorIdService;
        this.gerarLisuraDaFaturaService = gerarLisuraDaFaturaService;
    }

    @GetMapping("/faturas/{id}/lisura")
    public ResponseEntity<?> consultarLisura(@PathVariable Long id) {
        FaturaModel fatura = buscarFaturaPorIdService.buscarFaturaPorId(id);

        if (fatura == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FATURA_NAO_ENCONTRADA");
        }

        LisuraResponse resposta = gerarLisuraDaFaturaService.gerarLisuraDaFatura(fatura);
        return ResponseEntity.ok(resposta);
    }
}