package com.datasphere.fusex.fatura.controller;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.service.BuscarEspelhoPorIdService;
import com.datasphere.fusex.fatura.request.FaturaRequest;
import com.datasphere.fusex.fatura.request.FaturaResponse;
import com.datasphere.fusex.fatura.service.GerarFaturaDeEspelhoService;
import com.datasphere.fusex.fatura.service.ValidarEspelhoFaturavelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaturaController {

    private final BuscarEspelhoPorIdService buscarEspelhoPorIdService;
    private final ValidarEspelhoFaturavelService validarEspelhoFaturavelService;
    private final GerarFaturaDeEspelhoService gerarFaturaDeEspelhoService;

    public FaturaController(BuscarEspelhoPorIdService buscarEspelhoPorIdService,
                            ValidarEspelhoFaturavelService validarEspelhoFaturavelService,
                            GerarFaturaDeEspelhoService gerarFaturaDeEspelhoService) {
        this.buscarEspelhoPorIdService = buscarEspelhoPorIdService;
        this.validarEspelhoFaturavelService = validarEspelhoFaturavelService;
        this.gerarFaturaDeEspelhoService = gerarFaturaDeEspelhoService;
    }

    @PostMapping("/faturas")
    public ResponseEntity<?> gerarFatura(@RequestBody FaturaRequest request) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(request.espelhoId());
        if (espelho == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("espelho não encontrado");
        }

        boolean faturavel = validarEspelhoFaturavelService.validarEspelhoFaturavel(espelho);
        if (!faturavel) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("espelho já foi faturado ou está cancelado");
        }

        FaturaResponse response = gerarFaturaDeEspelhoService.gerarFaturaDeEspelho(espelho);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}