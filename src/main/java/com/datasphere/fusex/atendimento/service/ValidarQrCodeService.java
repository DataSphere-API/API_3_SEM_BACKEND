package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;

@Service
public class ValidarQrCodeService {

    private GuiaProcRepository guiaProcRepository;

    public ValidarQrCodeService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public GuiaProcModel validarQrCode (String qr) {
        return guiaProcRepository.findByQr(qr);
    }

}
