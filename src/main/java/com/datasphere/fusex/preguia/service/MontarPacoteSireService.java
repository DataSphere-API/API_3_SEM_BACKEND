package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.medico.MedicoModel;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.PacoteSireResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class MontarPacoteSireService {

    private final BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    private final PreGuiaProcRepository preGuiaProcRepository;
    private final MedicoRepository medicoRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService;
    private final CalcularHashPacoteService calcularHashPacoteService;
    private final ValidadeGuiaService validadeGuiaService;

    public MontarPacoteSireService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                                   PreGuiaProcRepository preGuiaProcRepository,
                                   MedicoRepository medicoRepository,
                                   BeneficiarioRepository beneficiarioRepository,
                                   CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService,
                                   CalcularHashPacoteService calcularHashPacoteService,
                                   ValidadeGuiaService validadeGuiaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.preGuiaProcRepository = preGuiaProcRepository;
        this.medicoRepository = medicoRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.calcularValorTotalPreGuiaService = calcularValorTotalPreGuiaService;
        this.calcularHashPacoteService = calcularHashPacoteService;
        this.validadeGuiaService = validadeGuiaService;
    }

    @Transactional(readOnly = true)
    public PacoteSireResponse montarPacote(Long preGuiaId) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaPorId(preGuiaId);

        boolean aprovada = preGuia.getStatus() == StatusPreGuia.APROVADA
                || preGuia.getStatus() == StatusPreGuia.EMITIDA;
        if (!aprovada) {
            throw new BusinessException(ErrorCode.PRE_GUIA_NAO_APROVADA);
        }

        PacoteSireResponse.AprovadoPor aprovadoPor = new PacoteSireResponse.AprovadoPor(
                preGuia.getChefeId(), preGuia.getDataAprovacaoChefe(), preGuia.getDataAprovacaoMedico());
        PacoteSireResponse.Beneficiario beneficiario = montarBeneficiario(preGuia);
        PacoteSireResponse.Medico medico = montarMedico(preGuia);

        List<PreGuiaProcModel> procedimentosDaPreGuia = preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(preGuiaId);
        List<PacoteSireResponse.Procedimento> procedimentos = montarProcedimentos(procedimentosDaPreGuia);
        BigDecimal valorTotal = calcularValorTotalPreGuiaService.calcularValorTotal(procedimentosDaPreGuia);

        String hash = calcularHashPacoteService.calcularHash(preGuiaId, beneficiario, medico, aprovadoPor, procedimentos);

        return new PacoteSireResponse(
                "1.0",
                preGuia.getId(),
                preGuia.getStatus().name(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                aprovadoPor,
                beneficiario,
                medico,
                procedimentos,
                valorTotal,
                validadeGuiaService.getValidadeDias(),
                hash);
    }

    private PacoteSireResponse.Beneficiario montarBeneficiario(PreGuiaModel preGuia) {
        BeneficiarioModel b = preGuia.getBeneficiarioModel();
        return new PacoteSireResponse.Beneficiario(
                b.getPrecCP(), b.getNome(), b.getDataNasc(), b.isDependente(), b.getGrupo(), b.getUg());
    }

    private PacoteSireResponse.Medico montarMedico(PreGuiaModel preGuia) {
        MedicoModel medico = medicoRepository.findById(preGuia.getMedId()).orElse(null);
        if (medico == null) {
            return new PacoteSireResponse.Medico(null, null);
        }
        BeneficiarioModel pessoa = beneficiarioRepository.findByPrecCP(medico.getBenefId()).orElse(null);
        String nome = null;
        if (pessoa != null) {
            nome = pessoa.getNome();
        }
        return new PacoteSireResponse.Medico(medico.getCrm(), nome);
    }

    private List<PacoteSireResponse.Procedimento> montarProcedimentos(List<PreGuiaProcModel> procedimentosDaPreGuia) {
        List<PacoteSireResponse.Procedimento> procedimentos = new ArrayList<>();
        for (PreGuiaProcModel p : procedimentosDaPreGuia) {
            procedimentos.add(new PacoteSireResponse.Procedimento(
                    p.getId(),
                    p.getProcedimentoModel().getNome(),
                    p.getOcsModel().getCnpj(),
                    p.getOcsModel().getNome(),
                    p.getQtdAutorizada(),
                    calcularValorTotalPreGuiaService.calcularValorUnitario(p),
                    calcularValorTotalPreGuiaService.calcularValorDoProcedimento(p)));
        }
        return procedimentos;
    }
}