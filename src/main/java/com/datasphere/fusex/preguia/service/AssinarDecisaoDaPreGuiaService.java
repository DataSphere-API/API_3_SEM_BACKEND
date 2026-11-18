package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Aprovador;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AssinarDecisaoDaPreGuiaService {

    // O nome é copiado para a assinatura continuar a mesma se o cadastro mudar depois.
    public void assinar(PreGuiaModel preGuia, Aprovador aprovador, LocalDateTime dataHora) {
        preGuia.setAssinaturaUsuarioId(aprovador.usuarioId());
        preGuia.setAssinaturaNome(aprovador.nome());
        preGuia.setAssinaturaPerfil(aprovador.papel().name());
        preGuia.setAssinaturaData(dataHora);
    }
}
