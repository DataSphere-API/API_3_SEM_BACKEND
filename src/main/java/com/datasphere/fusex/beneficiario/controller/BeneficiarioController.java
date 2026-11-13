package com.datasphere.fusex.beneficiario.controller;

import com.datasphere.fusex.beneficiario.dto.BeneficiariosResponse;
import com.datasphere.fusex.beneficiario.service.BuscarBeneficiariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BeneficiarioController {

    private final BuscarBeneficiariosService buscarBeneficiariosService;

    public BeneficiarioController(BuscarBeneficiariosService buscarBeneficiariosService) {
        this.buscarBeneficiariosService = buscarBeneficiariosService;
    }

    @GetMapping("/beneficiarios")
    public ResponseEntity<BeneficiariosResponse> buscar(@RequestParam(required = false) String busca) {
        return ResponseEntity.ok(buscarBeneficiariosService.buscar(busca));
    }
}
