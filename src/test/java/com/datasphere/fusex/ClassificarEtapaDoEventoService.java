package com.datasphere.fusex;

import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.linhadotempo.dto.EtapaLinhaDoTempo;
import org.springframework.stereotype.Service;

@Service
public class ClassificarEtapaDoEventoService {

    public EtapaLinhaDoTempo classificar(EntidadeTipo tipo, String statusNovo) {
        return switch (tipo) {
            case PRE_GUIA -> etapaDaPreGuia(statusNovo);
            case GUIA -> "EMITIDA".equals(statusNovo)
                    ? EtapaLinhaDoTempo.EMISSAO
                    : EtapaLinhaDoTempo.ATENDIMENTO;
            case GUIA_PROC -> EtapaLinhaDoTempo.ATENDIMENTO;
            case ESPELHO -> EtapaLinhaDoTempo.ESPELHO;
            case FATURA -> EtapaLinhaDoTempo.FATURA;
        };
    }

    private EtapaLinhaDoTempo etapaDaPreGuia(String statusNovo) {
        return switch (statusNovo) {
            case "APROVADA", "REPROVADA" -> EtapaLinhaDoTempo.APROVACAO;
            case "EMITIDA" -> EtapaLinhaDoTempo.EMISSAO;
            default -> EtapaLinhaDoTempo.SOLICITACAO;
        };
    }
}