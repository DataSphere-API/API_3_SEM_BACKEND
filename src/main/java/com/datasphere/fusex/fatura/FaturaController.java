package com.datasphere.fusex.fatura;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.service.BuscarEspelhoPorIdService;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.dto.FaturaRequest;
import com.datasphere.fusex.fatura.dto.FaturaResponse;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import com.datasphere.fusex.fatura.service.GerarFaturaDeEspelhoService;
import com.datasphere.fusex.fatura.service.ListarFaturasPorStatusService;
import com.datasphere.fusex.fatura.service.MontarFaturaResponseService;
import com.datasphere.fusex.fatura.service.RegerarFaturaDeEspelhoService;
import com.datasphere.fusex.fatura.service.ValidarEspelhoFaturavelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FaturaController {

    private final BuscarEspelhoPorIdService buscarEspelhoPorIdService;
    private final ValidarEspelhoFaturavelService validarEspelhoFaturavelService;
    private final GerarFaturaDeEspelhoService gerarFaturaDeEspelhoService;
    private final ListarFaturasPorStatusService listarFaturasPorStatusService;
    private final BuscarFaturaPorIdService buscarFaturaPorIdService;
    private final RegerarFaturaDeEspelhoService regerarFaturaDeEspelhoService;
    private final ItemFaturaRepository itemFaturaRepository;
    private final MontarFaturaResponseService montarFaturaResponseService;

    public FaturaController(
            BuscarEspelhoPorIdService buscarEspelhoPorIdService,
            ValidarEspelhoFaturavelService validarEspelhoFaturavelService,
            GerarFaturaDeEspelhoService gerarFaturaDeEspelhoService,
            ListarFaturasPorStatusService listarFaturasPorStatusService,
            BuscarFaturaPorIdService buscarFaturaPorIdService,
            RegerarFaturaDeEspelhoService regerarFaturaDeEspelhoService,
            ItemFaturaRepository itemFaturaRepository,
            MontarFaturaResponseService montarFaturaResponseService) {
        this.buscarEspelhoPorIdService = buscarEspelhoPorIdService;
        this.validarEspelhoFaturavelService = validarEspelhoFaturavelService;
        this.gerarFaturaDeEspelhoService = gerarFaturaDeEspelhoService;
        this.listarFaturasPorStatusService = listarFaturasPorStatusService;
        this.buscarFaturaPorIdService = buscarFaturaPorIdService;
        this.regerarFaturaDeEspelhoService = regerarFaturaDeEspelhoService;
        this.itemFaturaRepository = itemFaturaRepository;
        this.montarFaturaResponseService = montarFaturaResponseService;
    }

    @PostMapping("/faturas")
    public ResponseEntity<FaturaResponse> gerarFatura(@Valid @RequestBody FaturaRequest request) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(request.espelhoId());
        if (espelho == null) {
            throw new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO);
        }
        if (!validarEspelhoFaturavelService.validarEspelhoFaturavel(espelho)) {
            throw new BusinessException(ErrorCode.ESPELHO_NAO_FATURAVEL);
        }
        return ResponseEntity.ok(gerarFaturaDeEspelhoService.gerarFaturaDeEspelho(espelho));
    }

    @GetMapping("/faturas")
    public ResponseEntity<List<FaturaResponse>> listarFaturas(@RequestParam(required = false) StatusFatura status) {
        List<FaturaModel> faturas = listarFaturasPorStatusService.listarFaturasPorStatus(status);
        List<FaturaResponse> resposta = faturas.stream()
                .map(fatura -> montarFaturaResponseService.montar(fatura, itemFaturaRepository.findByFaturaId(fatura.getId())))
                .toList();
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/faturas/{id}")
    public ResponseEntity<FaturaResponse> buscarFatura(@PathVariable Long id) {
        FaturaModel fatura = buscarFaturaOuFalhar(id);
        return ResponseEntity.ok(montarFaturaResponseService.montar(fatura, itemFaturaRepository.findByFaturaId(id)));
    }

    @PostMapping("/faturas/{id}/regerar")
    public ResponseEntity<FaturaResponse> regerarFatura(@PathVariable Long id) {
        buscarFaturaOuFalhar(id);
        FaturaResponse novaFatura = regerarFaturaDeEspelhoService.regerarFaturaDeEspelho(id);
        if (novaFatura == null) {
            throw new BusinessException(ErrorCode.FATURA_NAO_REGERAVEL);
        }
        return ResponseEntity.ok(novaFatura);
    }

    private FaturaModel buscarFaturaOuFalhar(Long id) {
        FaturaModel fatura = buscarFaturaPorIdService.buscarFaturaPorId(id);
        if (fatura == null) {
            throw new BusinessException(ErrorCode.FATURA_NAO_ENCONTRADA);
        }
        return fatura;
    }
}
