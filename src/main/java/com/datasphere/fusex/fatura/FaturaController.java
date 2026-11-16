package com.datasphere.fusex.fatura;

import com.datasphere.fusex.fatura.dto.FaturaMensalDetalheResponse;
import com.datasphere.fusex.fatura.dto.FaturaMensalResumoResponse;
import com.datasphere.fusex.fatura.service.ConsultarFaturaMensalService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/faturas")
public class FaturaController {

    private final ConsultarFaturaMensalService consulta;

    public FaturaController(ConsultarFaturaMensalService consulta) {
        this.consulta = consulta;
    }

    @GetMapping
    public List<FaturaMensalResumoResponse> listar(
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String competencia,
            @RequestParam(required = false) StatusFatura status) {
        try {
            YearMonth mes = competencia == null || competencia.isBlank() ? null : YearMonth.parse(competencia);
            return consulta.listar(cnpj == null || cnpj.isBlank() ? null : cnpj.strip(), mes, status);
        } catch (DateTimeParseException erro) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
    }

    @GetMapping("/{id}")
    public FaturaMensalDetalheResponse buscarPorId(@PathVariable Long id) {
        return consulta.buscar(id);
    }
}
