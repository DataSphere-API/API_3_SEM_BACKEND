package com.datasphere.fusex.controller;

import com.datasphere.fusex.model.GuiaProc;
import com.datasphere.fusex.service.AtendimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AtendimentoController {

    @Autowired
    private AtendimentoService atendimentoService;

    @PostMapping ("/atendimentos/validar-qrcode")
    public GuiaProc validarQrCode (@RequestBody String qr) {
        return atendimentoService.registrarRealizacao(qr);
            }

            @GetMapping ("/ocs/{cnpj}/atendimentos")
                    public List<GuiaProc> ListarAtendimentosPorOcs (@PathVariable String cnpj) {
            return  atendimentoService.listarAtendimentosPorOcs(cnpj);

        }
    }
