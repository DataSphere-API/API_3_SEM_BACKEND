package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.atendimento.service.*;
import com.datasphere.fusex.dto.request.ConferenciaQrResponse;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.service.ValidacaoQrService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AtendimentoController {

    private final RegistrarRealizacaoService registrarRealizacaoService;
    private final ListarAtendimentosPorOcs listarAtendimentosPorOcs;
    private final ValidacaoQrService validacaoQrService;

    public AtendimentoController(RegistrarRealizacaoService registrarRealizacaoService,
                                 ListarAtendimentosPorOcs listarAtendimentosPorOcs,
                                 ValidacaoQrService validacaoQrService) {
        this.registrarRealizacaoService = registrarRealizacaoService;
        this.listarAtendimentosPorOcs = listarAtendimentosPorOcs;
        this.validacaoQrService = validacaoQrService;
    }

    @PostMapping("/atendimentos/validar-qrcode")
    public ResponseEntity<String> validarQrCode(@RequestBody String qr) {
        GuiaProcModel procedimento = registrarRealizacaoService.registrarRealizacao(qr);

        if (procedimento == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("QR code não encontrado");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body("qr code validado");
    }

    @GetMapping("/atendimentos/qrcode/{codigo}")
    public ResponseEntity<ConferenciaQrResponse> conferirQrCode(
            @PathVariable String codigo,
            @RequestParam String cnpj) {

        return ResponseEntity.ok(validacaoQrService.montarDadosConferenciaQr(codigo, cnpj));
    }

    @GetMapping("/ocs/{cnpj}/atendimentos")
    public ResponseEntity<?> ListarAtendimentosPorOcs(@PathVariable String cnpj) {
        List<GuiaProcModel> guiaProcModelList = listarAtendimentosPorOcs.listarAtendimentosPorOcs(cnpj);
        if (guiaProcModelList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("nenhum atendimento encontrado");
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(guiaProcModelList);
        }
    }
}