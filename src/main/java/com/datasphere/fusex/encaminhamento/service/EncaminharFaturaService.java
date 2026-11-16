package com.datasphere.fusex.encaminhamento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.encaminhamento.dto.EncaminharFaturaResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.service.ConsultarFaturaMensalService;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class EncaminharFaturaService {

    private final EntityManager entityManager;
    private final ConsultarFaturaMensalService consulta;
    private final ValidarMesEncerradoService validarMesEncerrado;
    private final RegistrarMudancaStatusService historico;

    public EncaminharFaturaService(EntityManager entityManager, ConsultarFaturaMensalService consulta,
                                   ValidarMesEncerradoService validarMesEncerrado,
                                   RegistrarMudancaStatusService historico) {
        this.entityManager = entityManager;
        this.consulta = consulta;
        this.validarMesEncerrado = validarMesEncerrado;
        this.historico = historico;
    }

    @Transactional
    public EncaminharFaturaResponse encaminharFatura(Long id) {
        FaturaModel fatura = entityManager.find(FaturaModel.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (fatura == null) throw new BusinessException(ErrorCode.FATURA_NAO_ENCONTRADA);
        if (fatura.getStatus() != StatusFatura.ATIVA) {
            throw new BusinessException(ErrorCode.FATURA_NAO_ENCAMINHAVEL);
        }

        YearMonth competencia = consulta.competenciaDaFatura(fatura);
        if (competencia == null) throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        validarMesEncerrado.validarMesEncerrado(competencia, LocalDate.now());
        if (!consulta.listarPendentes(fatura).isEmpty()) {
            throw new BusinessException(ErrorCode.AUDITORIA_PENDENTE);
        }

        fatura.setStatus(StatusFatura.ENCAMINHADA_APROVACAO);
        fatura.setDataEncaminhamento(LocalDateTime.now().withNano(0));
        historico.executar(EntidadeTipo.FATURA, id, StatusFatura.ATIVA.name(),
                fatura.getStatus().name(), "encaminhamento-para-aprovacao");

        return new EncaminharFaturaResponse(id, fatura.getStatus(), fatura.getDataEncaminhamento());
    }
}
