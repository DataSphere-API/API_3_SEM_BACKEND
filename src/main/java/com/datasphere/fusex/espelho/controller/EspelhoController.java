package com.datasphere.fusex.espelho.controller;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.request.EspelhoRequest;
import com.datasphere.fusex.espelho.request.EspelhoResponse;
import com.datasphere.fusex.espelho.request.ItemEspelhoRequest;
import com.datasphere.fusex.espelho.service.BuscarEspelhoPorIdService;
import com.datasphere.fusex.espelho.service.CorrigirItemEspelhoService;
import com.datasphere.fusex.espelho.service.CriarEspelhoComItensService;
import com.datasphere.fusex.espelho.service.ListarEspelhosPorOcsService;
import com.datasphere.fusex.espelho.service.ListarEspelhosPorStatusService;
import com.datasphere.fusex.espelho.service.ListarEspelhosService;
import com.datasphere.fusex.espelho.service.ListarItensDoEspelhoService;
import com.datasphere.fusex.espelho.service.RemoverItemDoEspelhoService;
import com.datasphere.fusex.espelho.service.ValidarAtendimentoRealizadoService;
import com.datasphere.fusex.espelho.service.ValidarEspelhoNaoDuplicadoPorAtendimentoService;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class EspelhoController {

    private final ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService;
    private final ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService;
    private final CriarEspelhoComItensService criarEspelhoComItensService;
    private final ListarEspelhosService listarEspelhosService;
    private final ListarEspelhosPorOcsService listarEspelhosPorOcsService;
    private final ListarEspelhosPorStatusService listarEspelhosPorStatusService;
    private final BuscarEspelhoPorIdService buscarEspelhoPorIdService;
    private final ListarItensDoEspelhoService listarItensDoEspelhoService;
    private final CorrigirItemEspelhoService corrigirItemEspelhoService;
    private final RemoverItemDoEspelhoService removerItemDoEspelhoService;

    public EspelhoController(ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService,
                             ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService,
                             CriarEspelhoComItensService criarEspelhoComItensService,
                             ListarEspelhosService listarEspelhosService,
                             ListarEspelhosPorOcsService listarEspelhosPorOcsService,
                             ListarEspelhosPorStatusService listarEspelhosPorStatusService,
                             BuscarEspelhoPorIdService buscarEspelhoPorIdService,
                             ListarItensDoEspelhoService listarItensDoEspelhoService,
                             CorrigirItemEspelhoService corrigirItemEspelhoService,
                             RemoverItemDoEspelhoService removerItemDoEspelhoService) {
        this.validarAtendimentoRealizadoService = validarAtendimentoRealizadoService;
        this.validarEspelhoNaoDuplicadoPorAtendimentoService = validarEspelhoNaoDuplicadoPorAtendimentoService;
        this.criarEspelhoComItensService = criarEspelhoComItensService;
        this.listarEspelhosService = listarEspelhosService;
        this.listarEspelhosPorOcsService = listarEspelhosPorOcsService;
        this.listarEspelhosPorStatusService = listarEspelhosPorStatusService;
        this.buscarEspelhoPorIdService = buscarEspelhoPorIdService;
        this.listarItensDoEspelhoService = listarItensDoEspelhoService;
        this.corrigirItemEspelhoService = corrigirItemEspelhoService;
        this.removerItemDoEspelhoService = removerItemDoEspelhoService;
    }

    @PostMapping("/espelhos")
    public ResponseEntity<?> criarEspelho(@RequestBody EspelhoRequest request) {
        GuiaProcModel atendimento = validarAtendimentoRealizadoService.validarAtendimentoRealizado(request.atendimentoId());
        if (atendimento == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("atendimento não encontrado ou ainda não realizado");
        }

        boolean naoDuplicado = validarEspelhoNaoDuplicadoPorAtendimentoService.validarEspelhoNaoDuplicadoPorAtendimento(request.atendimentoId());
        if (!naoDuplicado) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("já existe um espelho para este atendimento");
        }

        EspelhoResponse response = criarEspelhoComItensService.criarEspelhoComItens(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/espelhos")
    public ResponseEntity<List<EspelhoModel>> listarEspelhos(@RequestParam(required = false) String cnpj,
                                                             @RequestParam(required = false) StatusEspelho status) {
        if (cnpj != null && status != null) {
            List<EspelhoModel> porOcs = listarEspelhosPorOcsService.listarEspelhosPorOcs(cnpj);
            List<EspelhoModel> filtrados = new ArrayList<>();
            for (EspelhoModel espelho : porOcs) {
                if (espelho.getStatus() == status) {
                    filtrados.add(espelho);
                }
            }
            return ResponseEntity.ok(filtrados);
        }
        if (cnpj != null) {
            return ResponseEntity.ok(listarEspelhosPorOcsService.listarEspelhosPorOcs(cnpj));
        }
        if (status != null) {
            return ResponseEntity.ok(listarEspelhosPorStatusService.listarEspelhosPorStatus(status));
        }
        return ResponseEntity.ok(listarEspelhosService.listarEspelhos());
    }

    @GetMapping("/espelhos/{id}")
    public ResponseEntity<?> buscarEspelho(@PathVariable Integer id) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(id);
        if (espelho == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("espelho não encontrado");
        }
        return ResponseEntity.ok(espelho);
    }

    @GetMapping("/espelhos/{id}/itens")
    public ResponseEntity<?> listarItens(@PathVariable Integer id) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(id);
        if (espelho == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("espelho não encontrado");
        }
        List<EspelhoItemModel> itens = listarItensDoEspelhoService.listarItensDoEspelho(id);
        return ResponseEntity.ok(itens);
    }

    @PatchMapping("/espelhos/{id}/itens/{itemId}")
    public ResponseEntity<?> corrigirItem(@PathVariable Integer id,
                                          @PathVariable Integer itemId,
                                          @RequestBody ItemEspelhoRequest dados) {
        EspelhoItemModel item = corrigirItemEspelhoService.corrigirItemEspelho(id, itemId, dados);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("não foi possível corrigir o item");
        }
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/espelhos/{id}/itens/{itemId}")
    public ResponseEntity<?> removerItem(@PathVariable Integer id, @PathVariable Integer itemId) {
        boolean removido = removerItemDoEspelhoService.removerItemDoEspelho(id, itemId);
        if (!removido) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("não foi possível remover o item");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}