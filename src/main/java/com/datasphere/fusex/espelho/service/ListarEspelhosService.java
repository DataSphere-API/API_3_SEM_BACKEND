package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.EspelhoResumoResponse;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarEspelhosService {

    private final EspelhoRepository espelhoRepository;
    private final EnriquecerListaEspelhosService enriquecerListaEspelhosService;
    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    public ListarEspelhosService(EspelhoRepository espelhoRepository,
                                 EnriquecerListaEspelhosService enriquecerListaEspelhosService,
                                 BuscarUsuarioLogadoService buscarUsuarioLogadoService) {
        this.espelhoRepository = espelhoRepository;
        this.enriquecerListaEspelhosService = enriquecerListaEspelhosService;
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
    }

    // O auditor filtra por OCS e status; a OCS vê só os espelhos dela.
    @Transactional(readOnly = true)
    public List<EspelhoResumoResponse> listar(String cnpj, StatusEspelho status) {
        var usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();
        if (!usuario.tem(Perfil.AUDITOR)) {
            if (cnpj != null && !cnpj.equals(usuario.ocsCnpj())) {
                throw new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
            }
            cnpj = usuario.ocsCnpj();
        }
        List<EspelhoModel> espelhos = cnpj == null ? espelhoRepository.findAll() : espelhoRepository.findByOcsId(cnpj);
        if (status != null) {
            espelhos = espelhos.stream().filter(espelho -> espelho.getStatus() == status).toList();
        }
        return enriquecerListaEspelhosService.enriquecer(espelhos);
    }
}
