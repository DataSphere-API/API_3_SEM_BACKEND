package com.datasphere.fusex.encaminhamento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class ValidarMesEncerradoService {

    public void validarMesEncerrado(YearMonth competencia, LocalDate dataAtual) {
        LocalDate primeiroDiaDeEncaminhamento = competencia.plusMonths(1).atDay(6);
        if (dataAtual.isBefore(primeiroDiaDeEncaminhamento)) {
            throw new BusinessException(ErrorCode.MES_NAO_ENCERRADO);
        }
    }
}
