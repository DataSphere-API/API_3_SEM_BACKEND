package com.datasphere.fusex.preguia.controller;

import com.datasphere.fusex.preguia.dto.PreGuiaDetalheResponse;
import com.datasphere.fusex.preguia.dto.PreGuiaResumoResponse;
import com.datasphere.fusex.preguia.dto.ValidarPreGuiaResponse;
import com.datasphere.fusex.preguia.service.DetalharPreGuiaService;
import com.datasphere.fusex.preguia.service.ListarPreGuiasService;
import com.datasphere.fusex.preguia.service.ValidarPreGuiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PreGuiaController {

    private static final String HEADER_PREC_CP = "X-Prec-Cp";

    private final ListarPreGuiasService listarPreGuiasService;
    private final DetalharPreGuiaService detalharPreGuiaService;
    private final ValidarPreGuiaService validarPreGuiaService;

    public PreGuiaController(ListarPreGuiasService listarPreGuiasService,
                             DetalharPreGuiaService detalharPreGuiaService,
                             ValidarPreGuiaService validarPreGuiaService) {
        this.listarPreGuiasService = listarPreGuiasService;
        this.detalharPreGuiaService = detalharPreGuiaService;
        this.validarPreGuiaService = validarPreGuiaService;
    }

    @GetMapping("/pre-guias")
    public ResponseEntity<List<PreGuiaResumoResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestHeader(value = HEADER_PREC_CP, required = false) String precCp) {
        return ResponseEntity.ok(listarPreGuiasService.listarPreGuiasPorStatus(status, precCp));
    }

    @GetMapping("/pre-guias/{id}")
    public ResponseEntity<PreGuiaDetalheResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(detalharPreGuiaService.detalhar(id));
    }

    @PostMapping("/pre-guias/{id}/validar")
    public ResponseEntity<ValidarPreGuiaResponse> validar(@PathVariable Long id) {
        return ResponseEntity.ok(validarPreGuiaService.validarPreGuia(id));
    }
}