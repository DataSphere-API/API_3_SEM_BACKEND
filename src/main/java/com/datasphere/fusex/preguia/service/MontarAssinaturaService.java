package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.dto.AssinaturaResponse;
import org.springframework.stereotype.Service;

@Service
public class MontarAssinaturaService {

    public AssinaturaResponse montar(PreGuiaModel preGuia) {
        if (preGuia.getAssinaturaData() == null) {
            return null;
        }
        return new AssinaturaResponse(
                preGuia.getAssinaturaUsuarioId(),
                preGuia.getAssinaturaNome(),
                preGuia.getAssinaturaPerfil(),
                preGuia.getAssinaturaData());
    }
}
