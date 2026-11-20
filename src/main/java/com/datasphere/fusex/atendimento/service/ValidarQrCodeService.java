package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
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
