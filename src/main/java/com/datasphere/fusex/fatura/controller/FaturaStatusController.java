package com.datasphere.fusex.fatura.controller;

import com.datasphere.fusex.fatura.service.TransicionarStatusDaFaturaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaturaStatusController {

    private final TransicionarStatusDaFaturaService transicoes;

    public FaturaStatusController(TransicionarStatusDaFaturaService transicoes) {
        this.transicoes = transicoes;
    }

    @PatchMapping("/faturas/{id}/aprovar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void aprovar(@PathVariable Long id) {
        transicoes.aprovarFatura(id);
    }

    @PatchMapping("/faturas/{id}/liquidar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void liquidar(@PathVariable Long id) {
        transicoes.liquidarFatura(id);
    }
}
