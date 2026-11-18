package com.datasphere.fusex.encaminhamento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class ValidarMesEncerradoService {

    // A fatura de um mês só pode ser encaminhada a partir do dia 6 do mês seguinte.
    public boolean mesEncerrado(String competencia, LocalDate hoje) {
        if (competencia == null) {
            return false;
        }
        LocalDate liberadaEm = YearMonth.parse(competencia).plusMonths(1).atDay(6);
        return !hoje.isBefore(liberadaEm);
    }

    public void validarMesEncerrado(String competencia, LocalDate hoje) {
        if (!mesEncerrado(competencia, hoje)) {
            throw new BusinessException(ErrorCode.MES_NAO_ENCERRADO);
        }
    }
}
