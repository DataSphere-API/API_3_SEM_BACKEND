package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.dto.request.GuiaBeneficiarioResponse;
import com.datasphere.fusex.guia.dto.request.ProcedimentoGuiaResponse;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class GuiaBeneficiarioService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final GuiaRepository guiaRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final ValidadeGuiaService validadeGuiaService;

    public GuiaBeneficiarioService(BeneficiarioRepository beneficiarioRepository,
                                   GuiaRepository guiaRepository,
                                   GuiaProcRepository guiaProcRepository,
                                   ValidadeGuiaService validadeGuiaService) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.guiaRepository = guiaRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.validadeGuiaService = validadeGuiaService;
    }

    @Transactional(readOnly = true)
    public List<GuiaBeneficiarioResponse> listarGuiasPorBeneficiario(String precCp) {
        buscarBeneficiarioPorPrecCp(precCp);

        List<GuiaBeneficiarioResponse> respostas = new ArrayList<>();
        for (GuiaModel guia : guiaRepository.findByBenfIdOrderByIdDesc(precCp)) {
            respostas.add(new GuiaBeneficiarioResponse(
                    guia.getId(),
                    guia.getStatus(),
                    guia.getDataEmissao(),
                    validadeGuiaService.calcularDiasRestantes(guia.getDataEmissao()),
                    listarProcedimentosDaGuia(guia.getId())));
        }
        return respostas;
    }

    void buscarBeneficiarioPorPrecCp(String precCp) {
        beneficiarioRepository.findByPrecCP(precCp)
                .orElseThrow(() -> new BusinessException(ErrorCode.BENEFICIARIO_NAO_ENCONTRADO));
    }

    List<ProcedimentoGuiaResponse> listarProcedimentosDaGuia(Long guiaId) {
        List<ProcedimentoGuiaResponse> procedimentos = new ArrayList<>();
        for (GuiaProcModel p : guiaProcRepository.findByGuiaModel_Id(guiaId)) {
            procedimentos.add(new ProcedimentoGuiaResponse(
                    p.getGuiaId(),
                    p.getContratoModel().getProcedimentoModel().getNome(),
                    new ProcedimentoGuiaResponse.Ocs(
                            p.getContratoModel().getOcsModel().getCnpj(),
                            p.getContratoModel().getOcsModel().getNome()),
                    p.getStatus(),
                    p.getQtdAutorizada(),
                    p.getQtdRealizada(),
                    p.getQr() != null));
        }
        return procedimentos;
    }
}