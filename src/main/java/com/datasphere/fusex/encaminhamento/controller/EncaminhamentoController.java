package com.datasphere.fusex.encaminhamento.controller;

import com.datasphere.fusex.encaminhamento.dto.EncaminharFaturaResponse;
import com.datasphere.fusex.encaminhamento.service.EncaminharFaturaService;
import com.datasphere.fusex.encaminhamento.service.ValidarAuditoriaConcluidaService;
import com.datasphere.fusex.encaminhamento.service.ValidarFaturaEncaminhavelService;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import com.datasphere.fusex.lisura.dto.LisuraResponse;
import com.datasphere.fusex.lisura.service.GerarLisuraDaFaturaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EncaminhamentoController {

    private final BuscarFaturaPorIdService buscarFaturaPorIdService;
    private final ValidarFaturaEncaminhavelService validarFaturaEncaminhavelService;
    private final GerarLisuraDaFaturaService gerarLisuraDaFaturaService;
    private final ValidarAuditoriaConcluidaService validarAuditoriaConcluidaService;
    private final EncaminharFaturaService encaminharFaturaService;

    public EncaminhamentoController(BuscarFaturaPorIdService buscarFaturaPorIdService,
                                    ValidarFaturaEncaminhavelService validarFaturaEncaminhavelService,
                                    GerarLisuraDaFaturaService gerarLisuraDaFaturaService,
                                    ValidarAuditoriaConcluidaService validarAuditoriaConcluidaService,
                                    EncaminharFaturaService encaminharFaturaService) {
        this.buscarFaturaPorIdService = buscarFaturaPorIdService;
        this.validarFaturaEncaminhavelService = validarFaturaEncaminhavelService;
        this.gerarLisuraDaFaturaService = gerarLisuraDaFaturaService;
        this.validarAuditoriaConcluidaService = validarAuditoriaConcluidaService;
        this.encaminharFaturaService = encaminharFaturaService;
    }

    @PostMapping("/faturas/{id}/encaminhar")
    public ResponseEntity<?> encaminharFatura(@PathVariable Long id) {
        FaturaModel fatura = buscarFaturaPorIdService.buscarFaturaPorId(id);

        if (fatura == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FATURA_NAO_ENCONTRADA");
        }

        if (!validarFaturaEncaminhavelService.validarFaturaEncaminhavel(fatura)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("FATURA_NAO_ENCAMINHAVEL");
        }

        LisuraResponse lisura = gerarLisuraDaFaturaService.gerarLisuraDaFatura(fatura);

        if (!validarAuditoriaConcluidaService.validarAuditoriaConcluida(lisura)) {
            return ResponseEntity.status(422).body("AUDITORIA_PENDENTE");
        }

        EncaminharFaturaResponse resposta = encaminharFaturaService.encaminharFatura(fatura);
        return ResponseEntity.ok(resposta);
    }
}