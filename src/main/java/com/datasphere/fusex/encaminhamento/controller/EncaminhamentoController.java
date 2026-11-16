package com.datasphere.fusex.encaminhamento.controller;

import com.datasphere.fusex.encaminhamento.dto.EncaminharFaturaResponse;
import com.datasphere.fusex.encaminhamento.service.EncaminharFaturaService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EncaminhamentoController {

    private final EncaminharFaturaService encaminharFaturaService;

    public EncaminhamentoController(EncaminharFaturaService encaminharFaturaService) {
        this.encaminharFaturaService = encaminharFaturaService;
    }

    @PostMapping("/faturas/{id}/encaminhar")
    public EncaminharFaturaResponse encaminharFatura(@PathVariable Long id) {
        return encaminharFaturaService.encaminharFatura(id);
    }
}
