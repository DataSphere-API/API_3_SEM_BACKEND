package com.datasphere.fusex.atendimento.service;

import org.springframework.stereotype.Service;

@Service
public class GerarCodigoQrService {

    private final GerarCodigoUnicoQrService gerarCodigoUnicoQrService;

    public GerarCodigoQrService(GerarCodigoUnicoQrService gerarCodigoUnicoQrService) {
        this.gerarCodigoUnicoQrService = gerarCodigoUnicoQrService;
    }

    public String gerarCodigoQr() {
        return gerarCodigoUnicoQrService.gerarCodigoUnicoQr();
    }
}
