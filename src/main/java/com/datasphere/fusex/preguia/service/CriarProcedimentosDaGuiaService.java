package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.guiaProc.StatusProcedimento;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CriarProcedimentosDaGuiaService {

    private final GuiaProcRepository guiaProcRepository;
    private final BuscarContratoDaPreGuiaProcService buscarContratoDaPreGuiaProcService;
    private final ValidadeGuiaService validadeGuiaService;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public CriarProcedimentosDaGuiaService(GuiaProcRepository guiaProcRepository,
                                           BuscarContratoDaPreGuiaProcService buscarContratoDaPreGuiaProcService,
                                           ValidadeGuiaService validadeGuiaService,
                                           RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.guiaProcRepository = guiaProcRepository;
        this.buscarContratoDaPreGuiaProcService = buscarContratoDaPreGuiaProcService;
        this.validadeGuiaService = validadeGuiaService;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    public List<GuiaProcModel> criarProcedimentos(GuiaModel guia, List<PreGuiaProcModel> procedimentosDaPreGuia,
                                                  LocalDate dataEmissao) {
        List<GuiaProcModel> criados = new ArrayList<>();

        for (PreGuiaProcModel procedimentoDaPreGuia : procedimentosDaPreGuia) {
            ContratoModel contrato = buscarContratoDaPreGuiaProcService.buscarContrato(procedimentoDaPreGuia);
            if (contrato == null) {
                throw new BusinessException(ErrorCode.PROCEDIMENTO_SEM_CONTRATO);
            }

            GuiaProcModel procedimento = new GuiaProcModel();
            procedimento.setContratoModel(contrato);
            procedimento.setGuiaModel(guia);
            procedimento.setStatus(StatusProcedimento.PENDENTE.name());
            procedimento.setQtdAutorizada(procedimentoDaPreGuia.getQtdAutorizada());
            procedimento.setQtdRealizada(0);
            procedimento.setDataValidade(validadeGuiaService.calcularDataValidade(dataEmissao));
            GuiaProcModel salvo = guiaProcRepository.save(procedimento);

            registrarMudancaStatusService.executar(EntidadeTipo.GUIA_PROC, salvo.getGuiaId(),
                    null, StatusProcedimento.PENDENTE.name(), "emissao-da-guia");
            criados.add(salvo);
        }
        return criados;
    }
}