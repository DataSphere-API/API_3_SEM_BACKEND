package com.datasphere.fusex.service;

import com.datasphere.fusex.model.GuiaProc;
import com.datasphere.fusex.repository.GuiaProcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtendimentoService {
    @Autowired
    private GuiaProcRepository guiaProcRepository;

    public GuiaProc validarQrCode (String qr) {
        return guiaProcRepository.findByQr(qr);
    }

    public GuiaProc atualizarStatusGuiaProcRealizada (GuiaProc procedimento) {
        procedimento.setStatus("Realizada");
        return guiaProcRepository.save (procedimento);
    }

    public GuiaProc registrarRealizacao (String qr) {
        GuiaProc procedimento = validarQrCode(qr);

        if (procedimento != null) {
            System.out.println("Procedimento encontrado. Atualizando");
            return  atualizarStatusGuiaProcRealizada(procedimento);
        } else {
            System.out.println(" Nenhum procedimento encontrado para o QR Code: " + qr);
            return null;
        }
    }

    public List<GuiaProc> listarAtendimentosPorOcs(String cnpj) {
        return guiaProcRepository.findAll();
    }

}
