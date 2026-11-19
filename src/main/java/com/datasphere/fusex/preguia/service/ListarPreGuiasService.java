package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.PreGuiaResumoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListarPreGuiasService {

    private final PreGuiaRepository preGuiaRepository;
    private final PreGuiaProcRepository preGuiaProcRepository;
    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;
    private final CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService;
    private final BuscarDataEmissaoDaPreGuiaService buscarDataEmissaoDaPreGuiaService;
    private final GuiaRepository guiaRepository;
    private final ValidadeGuiaService validadeGuiaService;

    public ListarPreGuiasService(PreGuiaRepository preGuiaRepository,
                                 PreGuiaProcRepository preGuiaProcRepository,
                                 BuscarUsuarioLogadoService buscarUsuarioLogadoService,
                                 CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService,
                                 BuscarDataEmissaoDaPreGuiaService buscarDataEmissaoDaPreGuiaService,
                                 GuiaRepository guiaRepository,
                                 ValidadeGuiaService validadeGuiaService) {
        this.preGuiaRepository = preGuiaRepository;
        this.preGuiaProcRepository = preGuiaProcRepository;
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
        this.calcularValorTotalPreGuiaService = calcularValorTotalPreGuiaService;
        this.buscarDataEmissaoDaPreGuiaService = buscarDataEmissaoDaPreGuiaService;
        this.guiaRepository = guiaRepository;
        this.validadeGuiaService = validadeGuiaService;
    }

    @Transactional(readOnly = true)
    public List<PreGuiaResumoResponse> listarPreGuiasPorStatus(String statusTexto, boolean somenteMinhas) {
        StatusPreGuia status = converterStatus(statusTexto);
        Integer medicoId = buscarIdDoMedicoParaFiltrar(somenteMinhas);

        List<PreGuiaModel> preGuias = buscarPreGuias(status, medicoId);

        List<PreGuiaResumoResponse> respostas = new ArrayList<>();
        for (PreGuiaModel preGuia : preGuias) {
            respostas.add(montarResumo(preGuia));
        }
        return respostas;
    }

    private StatusPreGuia converterStatus(String statusTexto) {
        if (statusTexto == null || statusTexto.isBlank()) {
            return null;
        }
        try {
            return StatusPreGuia.valueOf(statusTexto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.STATUS_INVALIDO);
        }
    }


    private Integer buscarIdDoMedicoParaFiltrar(boolean somenteMinhas) {
        UsuarioLogado usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();
        if (!usuario.tem(Perfil.MEDICO)) {
            return null;
        }
        boolean veTodas = usuario.tem(Perfil.CHEFE) || usuario.tem(Perfil.EMISSOR) || usuario.tem(Perfil.AUDITOR);
        if (veTodas && !somenteMinhas) {
            return null;
        }
        return usuario.medicoId();
    }

    private List<PreGuiaModel> buscarPreGuias(StatusPreGuia status, Integer medicoId) {
        if (medicoId != null && status != null) {
            return preGuiaRepository.findByMedIdAndStatusOrderByIdAsc(medicoId, status);
        }
        if (medicoId != null) {
            return preGuiaRepository.findByMedIdOrderByIdAsc(medicoId);
        }
        if (status != null) {
            return preGuiaRepository.findByStatusOrderByIdAsc(status);
        }
        return preGuiaRepository.findAllByOrderByIdAsc();
    }

    private PreGuiaResumoResponse montarResumo(PreGuiaModel preGuia) {
        List<PreGuiaProcModel> procedimentos = preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(preGuia.getId());
        LocalDate dataEmissao = buscarDataEmissaoDaPreGuiaService.buscarDataEmissao(preGuia);
        // Guia emitida: número oficial (ex.: 2026-000123) e validade calculada como no detalhe.
        String guiaNumero = null;
        if (preGuia.getGuiaId() != null) {
            guiaNumero = guiaRepository.findById(preGuia.getGuiaId()).map(GuiaModel::getNumero).orElse(null);
        }
        LocalDate validoAte = dataEmissao == null ? null : validadeGuiaService.calcularDataValidade(dataEmissao);
        return new PreGuiaResumoResponse(
                preGuia.getId(),
                preGuia.getBeneficiarioModel().getNome(),
                preGuia.getStatus(),
                procedimentos.size(),
                calcularValorTotalPreGuiaService.calcularValorTotal(procedimentos),
                preGuia.getDataCriacao(),
                preGuia.getDataAprovacaoMedico() != null,
                preGuia.getDataAprovacaoChefe() != null,
                dataEmissao,
                preGuia.getGuiaId(),
                preGuia.getBenfId(),
                guiaNumero,
                validoAte);
    }
}
