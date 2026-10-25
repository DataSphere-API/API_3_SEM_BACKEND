package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.atendimento.service.ListarAtendimentosPorOcs;
import com.datasphere.fusex.atendimento.service.RegistrarRealizacaoService;
import com.datasphere.fusex.atendimento.service.RegistrarSessaoService;
import com.datasphere.fusex.dto.request.AtendimentoResponse;
import com.datasphere.fusex.dto.request.BaixaResponse;
import com.datasphere.fusex.dto.request.ConferenciaQrResponse;
import com.datasphere.fusex.dto.request.SessaoRequest;
import com.datasphere.fusex.dto.request.SessaoResponse;
import com.datasphere.fusex.dto.request.ValidarQrCodeRequest;
import com.datasphere.fusex.service.ValidacaoQrService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AtendimentoController {

    private final RegistrarRealizacaoService registrarRealizacaoService;
    private final ListarAtendimentosPorOcs listarAtendimentosPorOcs;
    private final ValidacaoQrService validacaoQrService;
    private final RegistrarSessaoService registrarSessaoService;

    public AtendimentoController(RegistrarRealizacaoService registrarRealizacaoService,
                                 ListarAtendimentosPorOcs listarAtendimentosPorOcs,
                                 ValidacaoQrService validacaoQrService,
                                 RegistrarSessaoService registrarSessaoService) {
        this.registrarRealizacaoService = registrarRealizacaoService;
        this.listarAtendimentosPorOcs = listarAtendimentosPorOcs;
        this.validacaoQrService = validacaoQrService;
        this.registrarSessaoService = registrarSessaoService;
    }

    @PostMapping("/atendimentos/validar-qrcode")
    public ResponseEntity<BaixaResponse> validarQrCode(@RequestBody ValidarQrCodeRequest request) {
        return ResponseEntity.ok(registrarRealizacaoService.registrarRealizacao(request));
    }

    @PostMapping("/atendimentos/{id}/sessoes")
    public ResponseEntity<SessaoResponse> registrarSessao(
            @PathVariable Long id,
            @RequestBody SessaoRequest request) {

        SessaoResponse response = registrarSessaoService.registrarSessao(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/atendimentos/qrcode/{codigo}")
    public ResponseEntity<ConferenciaQrResponse> conferirQrCode(
            @PathVariable String codigo,
            @RequestParam String cnpj) {

        return ResponseEntity.ok(validacaoQrService.montarDadosConferenciaQr(codigo, cnpj));
    }

    @GetMapping("/ocs/{cnpj}/atendimentos")
    public ResponseEntity<List<AtendimentoResponse>> listarAtendimentosPorOcs(@PathVariable String cnpj) {
        return ResponseEntity.ok(listarAtendimentosPorOcs.listarAtendimentosPorOcs(cnpj));
    }
}