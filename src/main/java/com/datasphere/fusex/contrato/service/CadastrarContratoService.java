package com.datasphere.fusex.contrato.service;

import com.datasphere.fusex.chefe.service.ValidarChefeService;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.contrato.ContratoRepository;
import com.datasphere.fusex.contrato.dto.ContratoRequest;
import com.datasphere.fusex.contrato.dto.ContratoResponse;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.ocs.OcsRepository;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import com.datasphere.fusex.procedimento.ProcedimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;

@Service
public class CadastrarContratoService {

    private final ContratoRepository contratoRepository;
    private final OcsRepository ocsRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final ValidarChefeService validarChefeService;

    public CadastrarContratoService(ContratoRepository contratoRepository, OcsRepository ocsRepository,
                                     ProcedimentoRepository procedimentoRepository,
                                     ValidarChefeService validarChefeService) {
        this.contratoRepository = contratoRepository;
        this.ocsRepository = ocsRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.validarChefeService = validarChefeService;
    }

    @Transactional
    public ContratoResponse cadastrar(ContratoRequest request, Principal principal) {
        validarChefeService.validar(principal);
        if (request == null || request.valor() == null || request.valor().signum() <= 0) {
            throw new BusinessException(ErrorCode.VALOR_INVALIDO);
        }
        BigDecimal valor;
        try {
            valor = request.valor().setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new BusinessException(ErrorCode.VALOR_INVALIDO);
        }
        if (valor.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new BusinessException(ErrorCode.VALOR_INVALIDO);
        }
        if (request.ocsCnpj() == null || request.procedimentoId() == null) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
        String cnpj = request.ocsCnpj().replaceAll("[.\\-/\\s]", "");
        OcsModel ocs = ocsRepository.findById(cnpj)
                .orElseThrow(() -> new BusinessException(ErrorCode.OCS_NAO_ENCONTRADA));
        ProcedimentoModel procedimento = procedimentoRepository.findById(request.procedimentoId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROCEDIMENTO_NAO_ENCONTRADO));
        if (contratoRepository.existsByProcedimentoModel_IdAndOcsModel_Cnpj(procedimento.getId(), cnpj)) {
            throw new BusinessException(ErrorCode.CONTRATO_DUPLICADO);
        }
        ContratoModel contrato = new ContratoModel(null, procedimento, ocs, valor);
        return ContratoResponse.de(contratoRepository.saveAndFlush(contrato));
    }
}
