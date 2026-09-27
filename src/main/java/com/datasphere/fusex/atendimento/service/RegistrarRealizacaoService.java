package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import org.springframework.stereotype.Service;

@Service
public class RegistrarRealizacaoService {

    private AtualizarStatusGuiaProcRealizadaService atualizarStatusGuiaProcRealizada;
    private ValidarQrCodeService validarQrCode;

    public RegistrarRealizacaoService(AtualizarStatusGuiaProcRealizadaService atualizarStatusGuiaProcRealizada, ValidarQrCodeService validarQrCode) {
        this.atualizarStatusGuiaProcRealizada = atualizarStatusGuiaProcRealizada;
        this.validarQrCode = validarQrCode;
    }

    public GuiaProcModel registrarRealizacao (String qr) {
        GuiaProcModel procedimento = validarQrCode.validarQrCode(qr);

        if (procedimento != null) {
            System.out.println("Procedimento encontrado. Atualizando");
            return atualizarStatusGuiaProcRealizada.atualizarStatusGuiaProcRealizada(procedimento);
        } else {
            System.out.println(" Nenhum procedimento encontrado para o QR Code: " + qr);
            return null;
        }
    }

}
