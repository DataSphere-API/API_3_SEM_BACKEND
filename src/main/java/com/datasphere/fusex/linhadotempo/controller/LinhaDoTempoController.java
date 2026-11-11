package com.datasphere.fusex.linhadotempo.controller;

import com.datasphere.fusex.linhadotempo.dto.LinhaDoTempoResponse;
import com.datasphere.fusex.linhadotempo.service.MontarLinhaDoTempoDaGuiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LinhaDoTempoController {

    private final MontarLinhaDoTempoDaGuiaService montarLinhaDoTempoDaGuiaService;

    public LinhaDoTempoController(MontarLinhaDoTempoDaGuiaService montarLinhaDoTempoDaGuiaService) {
        this.montarLinhaDoTempoDaGuiaService = montarLinhaDoTempoDaGuiaService;
    }

    @GetMapping("/guias/{id}/linha-do-tempo")
    public ResponseEntity<LinhaDoTempoResponse> linhaDoTempo(@PathVariable Long id) {
        return ResponseEntity.ok(montarLinhaDoTempoDaGuiaService.montar(id));
    }
}