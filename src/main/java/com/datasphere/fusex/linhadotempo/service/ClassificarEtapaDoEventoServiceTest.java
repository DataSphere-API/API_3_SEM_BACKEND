package com.datasphere.fusex.linhadotempo.service;

import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.linhadotempo.dto.EtapaLinhaDoTempo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassificarEtapaDoEventoServiceTest {

    private final ClassificarEtapaDoEventoService service = new ClassificarEtapaDoEventoService();

    @Test
    void preGuiaValidada_eSolicitacao() {
        assertEquals(EtapaLinhaDoTempo.SOLICITACAO, service.classificar(EntidadeTipo.PRE_GUIA, "VALIDADA"));
    }

    @Test
    void preGuiaAprovadaOuReprovada_eAprovacao() {
        assertEquals(EtapaLinhaDoTempo.APROVACAO, service.classificar(EntidadeTipo.PRE_GUIA, "APROVADA"));
        assertEquals(EtapaLinhaDoTempo.APROVACAO, service.classificar(EntidadeTipo.PRE_GUIA, "REPROVADA"));
    }

    @Test
    void preGuiaEmitidaEGuiaEmitida_saoEmissao() {
        assertEquals(EtapaLinhaDoTempo.EMISSAO, service.classificar(EntidadeTipo.PRE_GUIA, "EMITIDA"));
        assertEquals(EtapaLinhaDoTempo.EMISSAO, service.classificar(EntidadeTipo.GUIA, "EMITIDA"));
    }

    @Test
    void guiaFinalizadaEProcedimento_saoAtendimento() {
        assertEquals(EtapaLinhaDoTempo.ATENDIMENTO, service.classificar(EntidadeTipo.GUIA, "FINALIZADA"));
        assertEquals(EtapaLinhaDoTempo.ATENDIMENTO, service.classificar(EntidadeTipo.GUIA_PROC, "REALIZADA"));
    }

    @Test
    void espelhoEFatura_temSuasEtapas() {
        assertEquals(EtapaLinhaDoTempo.ESPELHO, service.classificar(EntidadeTipo.ESPELHO, "EM_ABERTO"));
        assertEquals(EtapaLinhaDoTempo.FATURA, service.classificar(EntidadeTipo.FATURA, "ATIVA"));
    }
}