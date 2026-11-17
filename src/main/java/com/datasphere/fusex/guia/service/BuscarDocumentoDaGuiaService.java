package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.dto.DocumentoGuia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuscarDocumentoDaGuiaService {

    private final GuiaRepository guiaRepository;

    public BuscarDocumentoDaGuiaService(GuiaRepository guiaRepository) {
        this.guiaRepository = guiaRepository;
    }

    @Transactional(readOnly = true)
    public DocumentoGuia buscar(Long guiaId) {
        GuiaModel guia = guiaRepository.findById(guiaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GUIA_NAO_ENCONTRADA));
        if (guia.getDocumento() == null || guia.getHashPacote() == null) {
            throw new BusinessException(ErrorCode.DOCUMENTO_GUIA_INDISPONIVEL);
        }
        return new DocumentoGuia(guia.getDocumento(), guia.getHashPacote());
    }
}
