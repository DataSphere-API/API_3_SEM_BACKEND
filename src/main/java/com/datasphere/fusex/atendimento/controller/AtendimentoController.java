package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.atendimento.service.AtendimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AtendimentoController {

    @Autowired
    private AtendimentoService atendimentoService;

    @PostMapping ("/atendimentos/validar-qrcode")
    public GuiaProcModel validarQrCode (@RequestBody String qr) {
        return atendimentoService.registrarRealizacao(qr);
            }

            @GetMapping ("/ocs/{cnpj}/atendimentos")
                    public List<GuiaProcModel> ListarAtendimentosPorOcs (@PathVariable String cnpj) {
            return  atendimentoService.listarAtendimentosPorOcs(cnpj);

        }
    }
