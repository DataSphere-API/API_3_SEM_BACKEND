package com.datasphere.fusex.fatura.controller;

import com.datasphere.fusex.fatura.dto.StatusDaFaturaResponse;
import com.datasphere.fusex.fatura.service.AlterarStatusDaFaturaService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaturaStatusController {

    private final AlterarStatusDaFaturaService alterarStatusDaFaturaService;

    public FaturaStatusController(AlterarStatusDaFaturaService alterarStatusDaFaturaService) {
        this.alterarStatusDaFaturaService = alterarStatusDaFaturaService;
    }

    @PatchMapping("/faturas/{id}/aprovar")
    public StatusDaFaturaResponse aprovar(@PathVariable Long id) {
        return alterarStatusDaFaturaService.aprovar(id);
    }

    @PatchMapping("/faturas/{id}/liquidar")
    public StatusDaFaturaResponse liquidar(@PathVariable Long id) {
        return alterarStatusDaFaturaService.liquidar(id);
    }
}
