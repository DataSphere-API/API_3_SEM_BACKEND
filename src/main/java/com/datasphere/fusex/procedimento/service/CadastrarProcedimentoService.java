package com.datasphere.fusex.procedimento.service;

import com.datasphere.fusex.chefe.service.ValidarChefeService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import com.datasphere.fusex.procedimento.ProcedimentoRepository;
import com.datasphere.fusex.procedimento.dto.ProcedimentoRequest;
import com.datasphere.fusex.procedimento.dto.ProcedimentoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;

@Service
public class CadastrarProcedimentoService {

    private final ProcedimentoRepository procedimentoRepository;
    private final ValidarChefeService validarChefeService;

    public CadastrarProcedimentoService(ProcedimentoRepository procedimentoRepository,
                                        ValidarChefeService validarChefeService) {
        this.procedimentoRepository = procedimentoRepository;
        this.validarChefeService = validarChefeService;
    }

    @Transactional
    public ProcedimentoResponse cadastrar(ProcedimentoRequest request, Principal principal) {
        validarChefeService.validar(principal);
        if (request == null || request.nome() == null || request.nome().isBlank()
                || request.nome().trim().length() > 150) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
        String nome = request.nome().trim();
        if (procedimentoRepository.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException(ErrorCode.PROCEDIMENTO_JA_CADASTRADO);
        }
        return ProcedimentoResponse.de(procedimentoRepository.saveAndFlush(new ProcedimentoModel(null, nome)));
    }
}
