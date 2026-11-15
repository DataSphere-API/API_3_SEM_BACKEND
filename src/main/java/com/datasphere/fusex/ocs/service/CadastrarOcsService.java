package com.datasphere.fusex.ocs.service;

import com.datasphere.fusex.chefe.service.ValidarChefeService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.ocs.OcsRepository;
import com.datasphere.fusex.ocs.dto.OcsRequest;
import com.datasphere.fusex.ocs.dto.OcsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;

@Service
public class CadastrarOcsService {

    private final OcsRepository ocsRepository;
    private final ValidarChefeService validarChefeService;

    public CadastrarOcsService(OcsRepository ocsRepository, ValidarChefeService validarChefeService) {
        this.ocsRepository = ocsRepository;
        this.validarChefeService = validarChefeService;
    }

    @Transactional
    public OcsResponse cadastrar(OcsRequest request, Principal principal) {
        validarChefeService.validar(principal);
        if (request == null || request.cnpj() == null || request.nome() == null
                || request.nome().isBlank() || request.nome().trim().length() > 150) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
        String cnpj = request.cnpj().replaceAll("[.\\-/\\s]", "");
        if (!cnpj.matches("[0-9]{14}")) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
        if (ocsRepository.existsById(cnpj)) {
            throw new BusinessException(ErrorCode.OCS_JA_CADASTRADA);
        }
        ocsRepository.inserir(cnpj, request.nome().trim());
        return new OcsResponse(cnpj, request.nome().trim());
    }
}
