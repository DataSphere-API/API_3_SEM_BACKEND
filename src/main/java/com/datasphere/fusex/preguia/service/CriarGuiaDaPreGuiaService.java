package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.StatusGuia;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.preguia.PreGuiaModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CriarGuiaDaPreGuiaService {

    private final GuiaRepository guiaRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public CriarGuiaDaPreGuiaService(GuiaRepository guiaRepository,
                                     RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.guiaRepository = guiaRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    public GuiaModel criarGuia(PreGuiaModel preGuia, LocalDate dataEmissao) {
        GuiaModel guia = new GuiaModel();
        guia.setBenfId(preGuia.getBenfId());
        guia.setMedId(preGuia.getMedId());
        guia.setEmissorId(preGuia.getEmissorId());
        guia.setChefeId(preGuia.getChefeId());
        guia.setDataEmissao(dataEmissao);
        guia.setStatus(StatusGuia.EMITIDA.name());
        GuiaModel salva = guiaRepository.save(guia);

        registrarMudancaStatusService.executar(EntidadeTipo.GUIA, salva.getId(),
                null, StatusGuia.EMITIDA.name(), "emissao-da-guia");
        return salva;
    }
}