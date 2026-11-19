package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.StatusPainelBeneficiario;
import com.datasphere.fusex.guia.dto.PreGuiaDoBeneficiarioResponse;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListarPreGuiasDoBeneficiarioService {

    private final PreGuiaRepository preGuiaRepository;
    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    public ListarPreGuiasDoBeneficiarioService(PreGuiaRepository preGuiaRepository,
                                               BuscarUsuarioLogadoService buscarUsuarioLogadoService) {
        this.preGuiaRepository = preGuiaRepository;
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
    }

    // Pré-guias em andamento (nem emitidas nem reprovadas): o beneficiário vê como "solicitada". Só as próprias.
    @Transactional(readOnly = true)
    public List<PreGuiaDoBeneficiarioResponse> listar(String precCp) {
        UsuarioLogado usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();
        if (usuario.precCp() == null || !usuario.precCp().equals(precCp)) {
            throw new BusinessException(ErrorCode.BENEFICIARIO_SEM_PERMISSAO);
        }

        List<PreGuiaDoBeneficiarioResponse> resposta = new ArrayList<>();
        for (PreGuiaModel preGuia : preGuiaRepository
                .findByBenfIdAndGuiaIdIsNullAndStatusNotInOrderByIdDesc(
                        precCp, List.of(StatusPreGuia.EMITIDA, StatusPreGuia.REPROVADA))) {
            resposta.add(new PreGuiaDoBeneficiarioResponse(
                    preGuia.getId(), StatusPainelBeneficiario.SOLICITADA, preGuia.getDataCriacao()));
        }
        return resposta;
    }
}
