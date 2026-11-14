package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.atendimento.service.BuscarQrDoProcedimentoService;
import com.datasphere.fusex.dto.request.QrCodeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
public class QrCodeController {

    private final BuscarQrDoProcedimentoService buscarQrDoProcedimentoService;

    public QrCodeController(BuscarQrDoProcedimentoService buscarQrDoProcedimentoService) {
        this.buscarQrDoProcedimentoService = buscarQrDoProcedimentoService;
    }

    @GetMapping("/atendimentos/{id}/qrcode")
    public ResponseEntity<QrCodeResponse> buscar(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(buscarQrDoProcedimentoService.buscarQrDoProcedimento(id, principal));
    }
}
