package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.dto.DocumentoGuia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class BuscarDocumentoDaGuiaService {

    private final GuiaRepository guiaRepository;
    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    public BuscarDocumentoDaGuiaService(GuiaRepository guiaRepository,
                                        BuscarUsuarioLogadoService buscarUsuarioLogadoService) {
        this.guiaRepository = guiaRepository;
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
    }

    // Emissor e chefe baixam qualquer guia; o beneficiário só a própria.
    @Transactional(readOnly = true)
    public DocumentoGuia buscar(Long guiaId) {
        GuiaModel guia = guiaRepository.findById(guiaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GUIA_NAO_ENCONTRADA));
        var usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();
        boolean fusex = usuario.tem(Perfil.EMISSOR) || usuario.tem(Perfil.CHEFE);
        if (!fusex && (usuario.precCp() == null || !Objects.equals(usuario.precCp(), guia.getBenfId()))) {
            throw new BusinessException(ErrorCode.BENEFICIARIO_SEM_PERMISSAO);
        }
        if (guia.getDocumento() == null || guia.getHashPacote() == null) {
            throw new BusinessException(ErrorCode.DOCUMENTO_GUIA_INDISPONIVEL);
        }
        return new DocumentoGuia(guia.getDocumento(), guia.getHashPacote());
    }
}
