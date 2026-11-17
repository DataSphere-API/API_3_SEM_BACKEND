package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.preguia.dto.PacoteSireResponse;
import org.springframework.stereotype.Service;

@Service
public class GuardarDocumentoDaGuiaService {

    private final MontarPacoteSireService montarPacoteSireService;
    private final GerarDocumentoConferenciaService gerarDocumentoConferenciaService;
    private final GuiaRepository guiaRepository;

    public GuardarDocumentoDaGuiaService(MontarPacoteSireService montarPacoteSireService,
                                         GerarDocumentoConferenciaService gerarDocumentoConferenciaService,
                                         GuiaRepository guiaRepository) {
        this.montarPacoteSireService = montarPacoteSireService;
        this.gerarDocumentoConferenciaService = gerarDocumentoConferenciaService;
        this.guiaRepository = guiaRepository;
    }

    public void guardarDocumentoDaGuia(GuiaModel guia, Long preGuiaId) {
        if (guia.getHashPacote() != null || guia.getDocumento() != null) {
            throw new BusinessException(ErrorCode.DOCUMENTO_GUIA_JA_GRAVADO);
        }

        PacoteSireResponse pacote = montarPacoteSireService.montarPacote(preGuiaId);
        guia.setDocumento(gerarDocumentoConferenciaService.gerarDocumento(pacote));
        guia.setHashPacote(pacote.hash());
        guiaRepository.save(guia);
    }
}
