package com.datasphere.fusex.fatura;

import com.datasphere.fusex.fatura.dto.FaturaResponse;
import com.datasphere.fusex.fatura.dto.ItemFaturaResponse;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import com.datasphere.fusex.fatura.service.ListarFaturasPorStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/faturas")
public class FaturaController {

    private final ListarFaturasPorStatusService listarFaturasPorStatusService;
    private final BuscarFaturaPorIdService buscarFaturaPorIdService;
    private final ItemFaturaRepository itemFaturaRepository;

    public FaturaController(ListarFaturasPorStatusService listarFaturasPorStatusService,
                            BuscarFaturaPorIdService buscarFaturaPorIdService,
                            ItemFaturaRepository itemFaturaRepository) {
        this.listarFaturasPorStatusService = listarFaturasPorStatusService;
        this.buscarFaturaPorIdService = buscarFaturaPorIdService;
        this.itemFaturaRepository = itemFaturaRepository;
    }

    @GetMapping
    public ResponseEntity<List<FaturaResponse>> listarPorStatus(@RequestParam(required = false) StatusFatura status) {
        List<FaturaResponse> faturas = listarFaturasPorStatusService.listar(status);
        return ResponseEntity.ok(faturas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        FaturaModel fatura = buscarFaturaPorIdService.buscarFaturaPorId(id);
        if (fatura == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("fatura não encontrada");
        }

        List<ItemFaturaModel> itens = itemFaturaRepository.findByFaturaId(id);
        List<ItemFaturaResponse> itensResponse = itens.stream()
                .map(ItemFaturaResponse::from)
                .toList();

        return ResponseEntity.ok(FaturaResponse.from(fatura, itens, null));
    }
}