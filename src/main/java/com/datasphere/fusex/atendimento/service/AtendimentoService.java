package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtendimentoService {
    @Autowired
    private GuiaProcRepository guiaProcRepository;

    public GuiaProcModel validarQrCode (String qr) {
        return guiaProcRepository.findByQr(qr);
    }

    public GuiaProcModel atualizarStatusGuiaProcRealizada (GuiaProcModel procedimento) {
        procedimento.setStatus("Realizada");
        return guiaProcRepository.save (procedimento);
    }

    public GuiaProcModel registrarRealizacao (String qr) {
        GuiaProcModel procedimento = validarQrCode(qr);

        if (procedimento != null) {
            System.out.println("Procedimento encontrado. Atualizando");
            return  atualizarStatusGuiaProcRealizada(procedimento);
        } else {
            System.out.println(" Nenhum procedimento encontrado para o QR Code: " + qr);
            return null;
        }
    }

    public List<GuiaProcModel> listarAtendimentosPorOcs(String cnpj) {
        return guiaProcRepository.findAll();
    }

}
