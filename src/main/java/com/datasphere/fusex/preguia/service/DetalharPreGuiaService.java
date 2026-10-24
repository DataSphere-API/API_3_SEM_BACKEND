package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.medico.MedicoModel;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.dto.PreGuiaDetalheResponse;
import com.datasphere.fusex.preguia.dto.PreGuiaProcedimentoResponse;
import com.datasphere.fusex.preguia.dto.ValidacaoPreGuiaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DetalharPreGuiaService {

    private final BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    private final PreGuiaProcRepository preGuiaProcRepository;
    private final MedicoRepository medicoRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final MontarProcedimentosDaPreGuiaService montarProcedimentosDaPreGuiaService;
    private final CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService;
    private final PreGuiaValidacaoService preGuiaValidacaoService;
    private final BuscarDataEmissaoDaPreGuiaService buscarDataEmissaoDaPreGuiaService;
    private final ValidadeGuiaService validadeGuiaService;

    public DetalharPreGuiaService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                                  PreGuiaProcRepository preGuiaProcRepository,
                                  MedicoRepository medicoRepository,
                                  BeneficiarioRepository beneficiarioRepository,
                                  MontarProcedimentosDaPreGuiaService montarProcedimentosDaPreGuiaService,
                                  CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService,
                                  PreGuiaValidacaoService preGuiaValidacaoService,
                                  BuscarDataEmissaoDaPreGuiaService buscarDataEmissaoDaPreGuiaService,
                                  ValidadeGuiaService validadeGuiaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.preGuiaProcRepository = preGuiaProcRepository;
        this.medicoRepository = medicoRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.montarProcedimentosDaPreGuiaService = montarProcedimentosDaPreGuiaService;
        this.calcularValorTotalPreGuiaService = calcularValorTotalPreGuiaService;
        this.preGuiaValidacaoService = preGuiaValidacaoService;
        this.buscarDataEmissaoDaPreGuiaService = buscarDataEmissaoDaPreGuiaService;
        this.validadeGuiaService = validadeGuiaService;
    }

    @Transactional(readOnly = true)
    public PreGuiaDetalheResponse detalhar(Long id) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaPorId(id);
        List<PreGuiaProcModel> procedimentos = preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(id);
        List<PreGuiaProcedimentoResponse> procedimentosResponse = montarProcedimentosDaPreGuiaService.montar(procedimentos);
        ValidacaoPreGuiaResponse validacao = preGuiaValidacaoService.consolidarPendencias(preGuia);

        LocalDate dataEmissao = buscarDataEmissaoDaPreGuiaService.buscarDataEmissao(preGuia);
        LocalDate validoAte = null;
        if (dataEmissao != null) {
            validoAte = validadeGuiaService.calcularDataValidade(dataEmissao);
        }

        return new PreGuiaDetalheResponse(
                preGuia.getId(),
                preGuia.getStatus(),
                montarBeneficiario(preGuia),
                montarMedico(preGuia),
                procedimentosResponse,
                calcularValorTotalPreGuiaService.calcularValorTotal(procedimentos),
                validacao,
                preGuia.getDataCriacao(),
                preGuia.getDataAprovacaoMedico(),
                preGuia.getDataAprovacaoChefe(),
                preGuia.getMotivoReprovacao(),
                dataEmissao,
                validoAte);
    }

    private PreGuiaDetalheResponse.Beneficiario montarBeneficiario(PreGuiaModel preGuia) {
        BeneficiarioModel beneficiario = preGuia.getBeneficiarioModel();
        return new PreGuiaDetalheResponse.Beneficiario(beneficiario.getPrecCP(), beneficiario.getNome());
    }

    private PreGuiaDetalheResponse.Medico montarMedico(PreGuiaModel preGuia) {
        MedicoModel medico = medicoRepository.findById(preGuia.getMedId()).orElse(null);
        if (medico == null) {
            return new PreGuiaDetalheResponse.Medico(preGuia.getMedId(), null, null);
        }
        BeneficiarioModel pessoa = beneficiarioRepository.findByPrecCP(medico.getBenefId()).orElse(null);
        String nome = null;
        if (pessoa != null) {
            nome = pessoa.getNome();
        }
        return new PreGuiaDetalheResponse.Medico(medico.getId(), nome, medico.getCrm());
    }
}