package com.datasphere.fusex.espelho.controller;

import com.datasphere.fusex.espelho.request.EspelhoRequest;
import com.datasphere.fusex.espelho.request.EspelhoResponse;
import com.datasphere.fusex.espelho.service.CriarEspelhoComItensService;
import com.datasphere.fusex.espelho.service.ValidarAtendimentoRealizadoService;
import com.datasphere.fusex.espelho.service.ValidarEspelhoNaoDuplicadoPorAtendimentoService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EspelhoController {

    private final ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService;
    private final ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService;
    private final CriarEspelhoComItensService criarEspelhoComItensService;

    public EspelhoController(ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService,
                             ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService,
                             CriarEspelhoComItensService criarEspelhoComItensService) {
        this.validarAtendimentoRealizadoService = validarAtendimentoRealizadoService;
        this.validarEspelhoNaoDuplicadoPorAtendimentoService = validarEspelhoNaoDuplicadoPorAtendimentoService;
        this.criarEspelhoComItensService = criarEspelhoComItensService;
    }

    @PostMapping("/espelhos")
    public ResponseEntity<?> criarEspelho(@RequestBody EspelhoRequest request) {
        GuiaProcModel atendimento = validarAtendimentoRealizadoService.validarAtendimentoRealizado(request.atendimentoId());
        if (atendimento == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Atendimento não encontrado ou realizado.");
        }

        boolean naoDuplicado = validarEspelhoNaoDuplicadoPorAtendimentoService.validarEspelhoNaoDuplicadoPorAtendimento(request.atendimentoId());
        if (!naoDuplicado) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Este atendimento já possui um espelho.");
        }

        EspelhoResponse response = criarEspelhoComItensService.criarEspelhoComItens(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}