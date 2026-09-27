package com.datasphere.fusex.historicoStatus;

import com.datasphere.fusex.historicoStatus.service.ListarHistoricoPorEntidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HistoricoStatusController {

    private final ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService;

    public HistoricoStatusController(ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService) {
        this.listarHistoricoPorEntidadeService = listarHistoricoPorEntidadeService;
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

    private List<HistoricoStatusResponse> buscar(EntidadeTipo tipo, Long id) {
        return listarHistoricoPorEntidadeService.executar(tipo, id)
                .stream()
                .map(HistoricoStatusResponse::from)
                .toList();
    }
}