package com.datasphere.fusex.atendimento.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GerarCodigoUnicoQrService {

    public String gerarCodigoUnicoQr() {
        return UUID.randomUUID().toString();
    }
}
