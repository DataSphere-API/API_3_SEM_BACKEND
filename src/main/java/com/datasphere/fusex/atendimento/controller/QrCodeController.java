package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.atendimento.service.GarantirQrDoProcedimentoService;
import com.datasphere.fusex.atendimento.service.GerarImagemQrCodeService;
import com.datasphere.fusex.dto.request.QrCodeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QrCodeController {

    private final GarantirQrDoProcedimentoService garantirQrDoProcedimentoService;
    private final GerarImagemQrCodeService gerarImagemQrCodeService;

    public QrCodeController(GarantirQrDoProcedimentoService garantirQrDoProcedimentoService,
                            GerarImagemQrCodeService gerarImagemQrCodeService) {
        this.garantirQrDoProcedimentoService = garantirQrDoProcedimentoService;
        this.gerarImagemQrCodeService = gerarImagemQrCodeService;
    }

    @GetMapping("/atendimentos/{id}/qrcode")
    public ResponseEntity<QrCodeResponse> gerar(@PathVariable Long id) {
        String codigo = garantirQrDoProcedimentoService.garantirQrDoProcedimento(id);
        return ResponseEntity.ok(new QrCodeResponse(id, codigo, gerarImagemQrCodeService.gerarImagemQrCode(codigo)));
    }
}
