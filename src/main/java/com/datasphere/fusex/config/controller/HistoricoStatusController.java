package com.datasphare.fusex.controller;

import com.datasphare.fusex.domain.enums.EntidadeTipo;
import com.datasphare.fusex.dto.HistoricoStatusResponse;
import com.datasphare.fusex.service.HistoricoStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HistoricoStatusController {

    private final HistoricoStatusService service;

    public HistoricoStatusController(HistoricoStatusService service) {
        this.service = service;
    }

    @GetMapping("/guias/{id}/historico")
    public ResponseEntity<List<HistoricoStatusResponse>> historicoGuia(@PathVariable Long id) {
        return ResponseEntity.ok(buscar(EntidadeTipo.GUIA_PROC, id));
    }

    @GetMapping("/espelhos/{id}/historico")
    public ResponseEntity<List<HistoricoStatusResponse>> historicoEspelho(@PathVariable Long id) {
        return ResponseEntity.ok(buscar(EntidadeTipo.ESPELHO, id));
    }

    @GetMapping("/faturas/{id}/historico")
    public ResponseEntity<List<HistoricoStatusResponse>> historicoFatura(@PathVariable Long id) {
        return ResponseEntity.ok(buscar(EntidadeTipo.FATURA, id));
    }

    @GetMapping("/{tipo}/historico")
    private List<HistoricoStatusResponse> buscar(EntidadeTipo tipo, Long id) {
        return service.listarHistoricoPorEntidade(tipo, id)
                .stream()
                .map(HistoricoStatusResponse::from)
                .toList();
    }
}