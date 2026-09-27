package com.datasphere.fusex.fatura;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.service.BuscarEspelhoPorIdService;
import com.datasphere.fusex.fatura.request.FaturaRequest;
import com.datasphere.fusex.fatura.request.FaturaResponse;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import com.datasphere.fusex.fatura.service.GerarFaturaDeEspelhoService;
import com.datasphere.fusex.fatura.service.ListarFaturasPorStatusService;
import com.datasphere.fusex.fatura.service.MontarFaturaResponseService;
import com.datasphere.fusex.fatura.service.RegerarFaturaDeEspelhoService;
import com.datasphere.fusex.fatura.service.ValidarEspelhoFaturavelService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> gerarFatura(@RequestBody FaturaRequest request) {

        EspelhoModel espelho =
                buscarEspelhoPorIdService.buscarEspelhoPorId(request.espelhoId());

        if (espelho == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("espelho não encontrado");
        }

        boolean faturavel =
                validarEspelhoFaturavelService.validarEspelhoFaturavel(espelho);

        if (!faturavel) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("espelho já foi faturado ou está cancelado");
        }

        FaturaResponse response =
                gerarFaturaDeEspelhoService.gerarFaturaDeEspelho(espelho);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/faturas")
    public ResponseEntity<List<FaturaResponse>> listarFaturas(
            @RequestParam(required = false) StatusFatura status) {

        List<FaturaModel> faturas =
                listarFaturasPorStatusService.listarFaturasPorStatus(status);

        List<FaturaResponse> resposta = faturas.stream()
                .map(fatura -> montarFaturaResponseService.montar(
                        fatura,
                        itemFaturaRepository.findByFaturaId(fatura.getId())
                ))
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/faturas/{id}")
    public ResponseEntity<?> buscarFatura(@PathVariable Long id) {

        FaturaModel fatura =
                buscarFaturaPorIdService.buscarFaturaPorId(id);

        if (fatura == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("fatura não encontrada");
        }

        FaturaResponse resposta =
                montarFaturaResponseService.montar(
                        fatura,
                        itemFaturaRepository.findByFaturaId(id)
                );

        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/faturas/{id}/regerar")
    public ResponseEntity<?> regerarFatura(@PathVariable Long id) {

        FaturaModel faturaExistente =
                buscarFaturaPorIdService.buscarFaturaPorId(id);

        if (faturaExistente == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("fatura não encontrada");
        }

        FaturaResponse novaFatura =
                regerarFaturaDeEspelhoService.regerarFaturaDeEspelho(id);

        if (novaFatura == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("não é possível regerar: a fatura já foi regerada ou o espelho não foi alterado após a geração");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(novaFatura);
    }
}