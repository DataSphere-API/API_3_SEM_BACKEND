package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.guia.service.MontarLinhaDoTempoDaGuiaService;
import com.datasphere.fusex.historicoStatus.service.HistoricoStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LinhaDoTempoController {

    private final MontarLinhaDoTempoDaGuiaService montarLinhaDoTempoDaGuiaService;

    public LinhaDoTempoController(MontarLinhaDoTempoDaGuiaService montarLinhaDoTempoDaGuiaService) {
        this.montarLinhaDoTempoDaGuiaService = montarLinhaDoTempoDaGuiaService;
    }

    @GetMapping("/guias/{id}/linha-do-tempo")
    public ResponseEntity<List<HistoricoStatusResponse>> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(montarLinhaDoTempoDaGuiaService.montar(id));
    }
}
