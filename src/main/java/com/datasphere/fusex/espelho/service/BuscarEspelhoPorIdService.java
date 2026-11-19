package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BuscarEspelhoPorIdService {

    private final EspelhoRepository espelhoRepository;
    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    public BuscarEspelhoPorIdService(EspelhoRepository espelhoRepository,
                                     BuscarUsuarioLogadoService buscarUsuarioLogadoService) {
        this.espelhoRepository = espelhoRepository;
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
    }

    public EspelhoModel buscarEspelhoPorId(Integer id) {
        return espelhoRepository.findById(id).orElse(null);
    }

    // 404 se não existe; a OCS só abre e altera os espelhos dela (403). O auditor abre qualquer um.
    public EspelhoModel buscarEspelhoVisivel(Integer id) {
        EspelhoModel espelho = espelhoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO));
        var usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();
        if (!usuario.tem(Perfil.AUDITOR) && !Objects.equals(espelho.getOcsId(), usuario.ocsCnpj())) {
            throw new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
        }
        return espelho;
    }
}
