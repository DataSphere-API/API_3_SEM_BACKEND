package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ValidarPeriodoDoEspelhoService {

    // Até este dia ainda vale mandar espelho do mês anterior.
    public static final int DIAS_DE_TOLERANCIA = 5;

    // O espelho é de um mês só: o atual ou, até o dia 5, o anterior. O fim não passa de hoje.
    public void validar(LocalDate dataInicio, LocalDate dataFim, LocalDate hoje) {
        if (dataFim.isBefore(dataInicio)) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS, List.of("dataFim: não pode ser antes de dataInicio"));
        }
        if (dataFim.isAfter(hoje)) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS, List.of("dataFim: não pode ser depois de hoje"));
        }
        YearMonth mes = YearMonth.from(dataInicio);
        if (!mes.equals(YearMonth.from(dataFim))) {
            throw new BusinessException(ErrorCode.FORA_DO_MES, List.of("dataInicio e dataFim precisam ser do mesmo mês"));
        }
        YearMonth atual = YearMonth.from(hoje);
        boolean anteriorAindaAberto = mes.equals(atual.minusMonths(1)) && hoje.getDayOfMonth() <= DIAS_DE_TOLERANCIA;
        if (!mes.equals(atual) && !anteriorAindaAberto) {
            throw new BusinessException(ErrorCode.FORA_DO_MES);
        }
    }
}
