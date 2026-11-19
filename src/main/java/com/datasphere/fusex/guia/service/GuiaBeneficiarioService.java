package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.dto.GuiaBeneficiarioResponse;
import com.datasphere.fusex.guia.dto.ProcedimentoGuiaResponse;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.guiaproc.StatusProcedimento;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.guia.StatusPainelBeneficiario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.security.Principal;

@Service
public class GuiaBeneficiarioService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final GuiaRepository guiaRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final ValidadeGuiaService validadeGuiaService;
    private final ValidarConsultaGuiasService validarConsultaGuiasService;
    private final ItemFaturaRepository itemFaturaRepository;

    public GuiaBeneficiarioService(BeneficiarioRepository beneficiarioRepository,
                                   GuiaRepository guiaRepository,
                                   GuiaProcRepository guiaProcRepository,
                                   ValidadeGuiaService validadeGuiaService,
                                   ValidarConsultaGuiasService validarConsultaGuiasService,
                                   ItemFaturaRepository itemFaturaRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.guiaRepository = guiaRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.validadeGuiaService = validadeGuiaService;
        this.validarConsultaGuiasService = validarConsultaGuiasService;
        this.itemFaturaRepository = itemFaturaRepository;
    }

    @Transactional(readOnly = true)
    public List<GuiaBeneficiarioResponse> listarGuiasPorBeneficiario(String precCp, Principal principal) {
        validarConsultaGuiasService.validarConsultaGuias(precCp, principal);
        buscarBeneficiarioPorPrecCp(precCp);

        List<GuiaBeneficiarioResponse> respostas = new ArrayList<>();
        for (GuiaModel guia : guiaRepository.findByBenfIdOrderByIdDesc(precCp)) {
            respostas.add(new GuiaBeneficiarioResponse(
                    guia.getId(),
                    guia.getNumero(),
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
                    p.getId(),
                    p.getContratoModel().getProcedimentoModel().getNome(),
                    new ProcedimentoGuiaResponse.Ocs(
                            p.getContratoModel().getOcsModel().getCnpj(),
                            p.getContratoModel().getOcsModel().getNome()),
                    p.getStatus(),
                    p.getQtdAutorizada(),
                    p.getQtdRealizada(),
                    p.getQr() != null,
                    statusPainelDoProcedimento(p)));
        }
        return procedimentos;
    }

    // Procedimento faturado mostra o andamento da fatura (aprovada ou liquidada).
    StatusPainelBeneficiario statusPainelDoProcedimento(GuiaProcModel procedimento) {
        StatusProcedimento status = StatusProcedimento.valueOf(procedimento.getStatus());
        StatusFatura statusDaFatura = null;
        if (status == StatusProcedimento.FATURADA) {
            List<StatusFatura> faturas = itemFaturaRepository.listarStatusDasFaturasDoAtendimento(procedimento.getId());
            if (!faturas.isEmpty()) {
                statusDaFatura = faturas.get(0);
            }
        }
        return StatusPainelBeneficiario.doProcedimento(status, statusDaFatura);
    }
}
