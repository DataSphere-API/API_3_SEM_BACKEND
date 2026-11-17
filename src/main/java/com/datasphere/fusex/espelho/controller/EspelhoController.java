package com.datasphere.fusex.espelho.controller;
import com.datasphere.fusex.exception.BusinessException;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.*;
import com.datasphere.fusex.espelho.service.*;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.dto.EspelhoItemResponse;
import com.datasphere.fusex.fatura.service.AlimentarFaturaNaAprovacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final EnriquecerListaEspelhosService enriquecerListaEspelhosService;
    private final AlimentarFaturaNaAprovacaoService alimentarFaturaNaAprovacaoService;

    public EspelhoController(ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService,
                             ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService,
                             CriarEspelhoComItensService criarEspelhoComItensService,
                             ListarEspelhosService listarEspelhosService,
                             ListarEspelhosPorOcsService listarEspelhosPorOcsService,
                             ListarEspelhosPorStatusService listarEspelhosPorStatusService,
                             BuscarEspelhoPorIdService buscarEspelhoPorIdService,
                             ListarItensDoEspelhoService listarItensDoEspelhoService,
                             CorrigirItemEspelhoService corrigirItemEspelhoService,
                             RemoverItemDoEspelhoService removerItemDoEspelhoService,
                             EnriquecerListaEspelhosService enriquecerListaEspelhosService,
                             AlimentarFaturaNaAprovacaoService alimentarFaturaNaAprovacaoService) {
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
        this.enriquecerListaEspelhosService = enriquecerListaEspelhosService;
        this.alimentarFaturaNaAprovacaoService = alimentarFaturaNaAprovacaoService;
    }

    @PostMapping("/espelhos")
    public ResponseEntity<EspelhoResponse> criarEspelho(@Valid @RequestBody EspelhoRequest request) {
        EspelhoResponse response = criarEspelhoComItensService.criarEspelhoComItens(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/espelhos")
    public ResponseEntity<List<EspelhoResumoResponse>> listarEspelhos(@RequestParam(required = false) String cnpj,
                                                                      @RequestParam(required = false) StatusEspelho status) {
        if (cnpj != null && status != null) {
            List<EspelhoModel> porOcs = listarEspelhosPorOcsService.listarEspelhosPorOcs(cnpj);
            List<EspelhoModel> filtrados = new ArrayList<>();
            for (EspelhoModel espelho : porOcs) {
                if (espelho.getStatus() == status) {
                    filtrados.add(espelho);
                }
            }
            return ResponseEntity.ok(enriquecerListaEspelhosService.enriquecer(filtrados));
        }
        if (cnpj != null) {
            return ResponseEntity.ok(enriquecerListaEspelhosService.enriquecer(listarEspelhosPorOcsService.listarEspelhosPorOcs(cnpj)));
        }
        if (status != null) {
            return ResponseEntity.ok(enriquecerListaEspelhosService.enriquecer(listarEspelhosPorStatusService.listarEspelhosPorStatus(status)));
        }
        return ResponseEntity.ok(listarEspelhosService.listarEspelhos());
    }

    @GetMapping("/espelhos/{id}")
    public ResponseEntity<EspelhoResumoResponse> buscarEspelho(@PathVariable Integer id) {
        EspelhoModel espelho = buscarEspelhoOuFalhar(id);
        return ResponseEntity.ok(enriquecerListaEspelhosService.enriquecer(List.of(espelho)).get(0));
    }

    @GetMapping("/espelhos/{id}/itens")
    public ResponseEntity<List<EspelhoItemResponse>> listarItens(@PathVariable Integer id) {
        buscarEspelhoOuFalhar(id);
        List<EspelhoItemResponse> itens = listarItensDoEspelhoService.listarItensDoEspelho(id).stream().map(EspelhoItemResponse::from).toList();
        return ResponseEntity.ok(itens);
    }

    @PatchMapping("/espelhos/{id}/itens/{itemId}")
    public ResponseEntity<EspelhoItemResponse> corrigirItem(@PathVariable Integer id,
                                                            @PathVariable Integer itemId,
                                                            @Valid @RequestBody EspelhoItemPatchRequest dados) {
        EspelhoItemModel item = corrigirItemEspelhoService.corrigirItemEspelho(id, itemId, dados);
        return ResponseEntity.ok(EspelhoItemResponse.from(item));
    }

    @DeleteMapping("/espelhos/{id}/itens/{itemId}")
    public ResponseEntity<Void> removerItem(@PathVariable Integer id, @PathVariable Integer itemId) {
        removerItemDoEspelhoService.removerItemDoEspelho(id, itemId);
        return ResponseEntity.noContent().build();
    }

    private EspelhoModel buscarEspelhoOuFalhar(Integer id) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(id);
        if (espelho == null) {
            throw new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO);
        }
        return espelho;
    }

    @PatchMapping("/espelhos/{id}/aprovar")
    public ResponseEntity<?> aprovarEspelho(@PathVariable Integer id) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(id);
        if (espelho == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("espelho não encontrado");
        }
        alimentarFaturaNaAprovacaoService.processarAprovacaoEspelho(espelho);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/espelhos/{id}/reprovar")
    public ResponseEntity<?> reprovarEspelho(@PathVariable Integer id) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(id);
        if (espelho == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("espelho não encontrado");
        }
        alimentarFaturaNaAprovacaoService.processarReprovacaoEspelho(espelho);
        return ResponseEntity.noContent().build();
    }
}
