package com.datasphere.fusex.guia.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiracaoAgendada {

    private final ExpirarGuiasService expirarGuiasService;

    public ExpiracaoAgendada(ExpirarGuiasService expirarGuiasService) {
        this.expirarGuiasService = expirarGuiasService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void executarTodaNoite() {
        expirarGuiasService.expirarGuiasVencidas();
    }
}