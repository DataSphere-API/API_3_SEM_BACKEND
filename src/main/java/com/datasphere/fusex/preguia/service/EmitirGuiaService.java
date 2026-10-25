package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.EmissaoProcedimentoResponse;
import com.datasphere.fusex.preguia.dto.EmissaoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmitirGuiaService {

    private final BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    private final PreGuiaProcRepository preGuiaProcRepository;
    private final CriarGuiaDaPreGuiaService criarGuiaDaPreGuiaService;
    private final CriarProcedimentosDaGuiaService criarProcedimentosDaGuiaService;
    private final RegistrarStatusPreGuiaService registrarStatusPreGuiaService;
    private final ValidadeGuiaService validadeGuiaService;

    public EmitirGuiaService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                             PreGuiaProcRepository preGuiaProcRepository,
                             CriarGuiaDaPreGuiaService criarGuiaDaPreGuiaService,
                             CriarProcedimentosDaGuiaService criarProcedimentosDaGuiaService,
                             RegistrarStatusPreGuiaService registrarStatusPreGuiaService,
                             ValidadeGuiaService validadeGuiaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.preGuiaProcRepository = preGuiaProcRepository;
        this.criarGuiaDaPreGuiaService = criarGuiaDaPreGuiaService;
        this.criarProcedimentosDaGuiaService = criarProcedimentosDaGuiaService;
        this.registrarStatusPreGuiaService = registrarStatusPreGuiaService;
        this.validadeGuiaService = validadeGuiaService;
    }

    @Transactional
    public EmissaoResponse emitir(Long preGuiaId) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(preGuiaId);

        if (preGuia.getStatus() == StatusPreGuia.EMITIDA) {
            throw new BusinessException(ErrorCode.PRE_GUIA_JA_EMITIDA);
        }
        if (preGuia.getStatus() != StatusPreGuia.APROVADA) {
            throw new BusinessException(ErrorCode.PRE_GUIA_NAO_APROVADA);
        }

        LocalDate dataEmissao = LocalDate.now();
        List<PreGuiaProcModel> procedimentosDaPreGuia =
                preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(preGuiaId);

        GuiaModel guia = criarGuiaDaPreGuiaService.criarGuia(preGuia, dataEmissao);
        List<GuiaProcModel> procedimentos =
                criarProcedimentosDaGuiaService.criarProcedimentos(guia, procedimentosDaPreGuia, dataEmissao);

        preGuia.setGuiaId(guia.getId());
        registrarStatusPreGuiaService.atualizarStatus(preGuia, StatusPreGuia.EMITIDA, "emissao-da-guia");

        List<EmissaoProcedimentoResponse> procedimentosResponse = new ArrayList<>();
        for (GuiaProcModel procedimento : procedimentos) {
            procedimentosResponse.add(new EmissaoProcedimentoResponse(procedimento.getGuiaId()));
        }

        return new EmissaoResponse(
                guia.getId(),
                guia.getStatus(),
                dataEmissao,
                validadeGuiaService.calcularDataValidade(dataEmissao),
                procedimentosResponse);
    }
}