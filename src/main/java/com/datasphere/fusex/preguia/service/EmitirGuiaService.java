package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
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
    private final GuardarDocumentoDaGuiaService guardarDocumentoDaGuiaService;

    public EmitirGuiaService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                             PreGuiaProcRepository preGuiaProcRepository,
                             CriarGuiaDaPreGuiaService criarGuiaDaPreGuiaService,
                             CriarProcedimentosDaGuiaService criarProcedimentosDaGuiaService,
                             RegistrarStatusPreGuiaService registrarStatusPreGuiaService,
                             ValidadeGuiaService validadeGuiaService,
                             GuardarDocumentoDaGuiaService guardarDocumentoDaGuiaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.preGuiaProcRepository = preGuiaProcRepository;
        this.criarGuiaDaPreGuiaService = criarGuiaDaPreGuiaService;
        this.criarProcedimentosDaGuiaService = criarProcedimentosDaGuiaService;
        this.registrarStatusPreGuiaService = registrarStatusPreGuiaService;
        this.validadeGuiaService = validadeGuiaService;
        this.guardarDocumentoDaGuiaService = guardarDocumentoDaGuiaService;
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

        // Depois do status EMITIDA para o PDF registrar a situação final.
        guardarDocumentoDaGuiaService.guardarDocumentoDaGuia(guia, preGuiaId);

        List<EmissaoProcedimentoResponse> procedimentosResponse = new ArrayList<>();
        for (GuiaProcModel procedimento : procedimentos) {
            procedimentosResponse.add(new EmissaoProcedimentoResponse(procedimento.getId()));
        }

        return new EmissaoResponse(
                guia.getId(),
                guia.getStatus(),
                dataEmissao,
                validadeGuiaService.calcularDataValidade(dataEmissao),
                procedimentosResponse);
    }
}
