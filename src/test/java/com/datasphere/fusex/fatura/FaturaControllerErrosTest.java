package com.datasphere.fusex.fatura;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.fatura.dto.FaturaMensalResumoResponse;
import com.datasphere.fusex.fatura.service.ConsultarFaturaMensalService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FaturaControllerErrosTest {

    private final ConsultarFaturaMensalService consulta = mock(ConsultarFaturaMensalService.class);
    private final org.springframework.test.web.servlet.MockMvc mvc =
            MockMvcBuilders.standaloneSetup(new FaturaController(consulta))
                    .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void encaminhaFiltrosDoContratoEDevolveResumo() throws Exception {
        String cnpj = "12345678000199";
        when(consulta.listar(cnpj, YearMonth.of(2026, 10), StatusFatura.ATIVA)).thenReturn(List.of(
                new FaturaMensalResumoResponse(1L, cnpj, "OCS de teste", "2026-10", StatusFatura.ATIVA,
                        1, 2, BigDecimal.TEN, BigDecimal.TEN, 0, null, null, true)));

        mvc.perform(get("/faturas").param("cnpj", cnpj).param("competencia", "2026-10").param("status", "ATIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].competencia").value("2026-10"))
                .andExpect(jsonPath("$[0].valorApresentado").value(10))
                .andExpect(jsonPath("$[0].podeEncaminhar").value(true));
    }

    @Test
    void faturaInexistenteDevolve404EmJson() throws Exception {
        when(consulta.buscar(9L)).thenThrow(new BusinessException(ErrorCode.FATURA_NAO_ENCONTRADA));

        mvc.perform(get("/faturas/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("FATURA_NAO_ENCONTRADA"));
    }
}
