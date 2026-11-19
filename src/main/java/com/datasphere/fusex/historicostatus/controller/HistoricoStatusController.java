package com.datasphere.fusex.historicostatus.controller;
import com.datasphere.fusex.historicostatus.EntidadeTipo;

import com.datasphere.fusex.historicostatus.dto.HistoricoStatusResponse;
import com.datasphere.fusex.historicostatus.service.ListarHistoricoPorEntidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HistoricoStatusController {

    private final ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService;

    public HistoricoStatusController(ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService) {
        this.listarHistoricoPorEntidadeService = listarHistoricoPorEntidadeService;
    }

    @GetMapping("/espelhos/{id}/historico")
    public ResponseEntity<List<HistoricoStatusResponse>> historicoEspelho(@PathVariable Long id) {
        return ResponseEntity.ok(buscar(EntidadeTipo.ESPELHO, id));
    }

    @GetMapping("/faturas/{id}/historico")
    public ResponseEntity<List<HistoricoStatusResponse>> historicoFatura(@PathVariable Long id) {
        return ResponseEntity.ok(buscar(EntidadeTipo.FATURA, id));
    }

    private List<HistoricoStatusResponse> buscar(EntidadeTipo tipo, Long id) {
        return listarHistoricoPorEntidadeService.executar(tipo, id)
                .stream()
                .map(HistoricoStatusResponse::from)
                .toList();
    }
}
