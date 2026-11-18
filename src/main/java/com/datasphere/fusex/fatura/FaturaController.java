package com.datasphere.fusex.fatura;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.dto.FaturaMensalDetalheResponse;
import com.datasphere.fusex.fatura.dto.FaturaMensalResumoResponse;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import com.datasphere.fusex.fatura.service.ListarFaturasMensaisService;
import com.datasphere.fusex.fatura.service.MontarFaturaMensalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/faturas")
public class FaturaController {

    private final ListarFaturasMensaisService listarFaturasMensaisService;
    private final BuscarFaturaPorIdService buscarFaturaPorIdService;
    private final MontarFaturaMensalService montarFaturaMensalService;

    public FaturaController(ListarFaturasMensaisService listarFaturasMensaisService,
                            BuscarFaturaPorIdService buscarFaturaPorIdService,
                            MontarFaturaMensalService montarFaturaMensalService) {
        this.listarFaturasMensaisService = listarFaturasMensaisService;
        this.buscarFaturaPorIdService = buscarFaturaPorIdService;
        this.montarFaturaMensalService = montarFaturaMensalService;
    }

    @GetMapping
    public List<FaturaMensalResumoResponse> listar(@RequestParam(required = false) String cnpj,
                                                   @RequestParam(required = false) String competencia,
                                                   @RequestParam(required = false) StatusFatura status) {
        return listarFaturasMensaisService.listar(cnpj, competencia, status);
    }

    @GetMapping("/{id}")
    public FaturaMensalDetalheResponse buscarPorId(@PathVariable Long id) {
        FaturaModel fatura = buscarFaturaPorIdService.buscarFaturaPorId(id);
        if (fatura == null) {
            throw new BusinessException(ErrorCode.FATURA_NAO_ENCONTRADA);
        }
        return montarFaturaMensalService.montarDetalhe(fatura);
    }
}
