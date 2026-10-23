package com.datasphere.fusex.preguia.controller;

import com.datasphere.fusex.preguia.dto.PreGuiaResumoResponse;
import com.datasphere.fusex.preguia.service.ListarPreGuiasPorStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PreGuiaController {

    private final ListarPreGuiasPorStatusService listarPreGuiasPorStatusService;

    public PreGuiaController(ListarPreGuiasPorStatusService listarPreGuiasPorStatusService) {
        this.listarPreGuiasPorStatusService = listarPreGuiasPorStatusService;
    }

    @GetMapping("/pre-guias")
    public ResponseEntity<?> listarPreGuias(@RequestParam(required = false) String status) {
        List<PreGuiaResumoResponse> preGuias = listarPreGuiasPorStatusService.listarPreGuiasPorStatus(status);

        if (preGuias == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("STATUS_INVALIDO");
        }

        return ResponseEntity.ok(preGuias);
    }
}