package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.atendimento.service.*;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.util.List;

@RestController
public class AtendimentoController {

    private RegistrarRealizacaoService registrarRealizacaoService;
    private ListarAtendimentosPorOcs listarAtendimentosPorOcs;

    public AtendimentoController(RegistrarRealizacaoService registrarRealizacaoService, ListarAtendimentosPorOcs listarAtendimentosPorOcs) {
        this.registrarRealizacaoService = registrarRealizacaoService;
        this.listarAtendimentosPorOcs = listarAtendimentosPorOcs;
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

    @GetMapping ("/ocs/{cnpj}/atendimentos")
    public ResponseEntity<?> ListarAtendimentosPorOcs (@PathVariable String cnpj) {
        List<GuiaProcModel> guiaProcModelList = listarAtendimentosPorOcs.listarAtendimentosPorOcs(cnpj);
        if (guiaProcModelList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("nenhum atendimento encontrado");
        }
        else{
            return ResponseEntity.status(HttpStatus.OK)
                    .body(guiaProcModelList);
            }
        }
    }
