package com.datasphere.fusex.espelho.controller;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.*;
import com.datasphere.fusex.espelho.service.*;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.dto.EspelhoItemResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import jakarta.validation.Valid;
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
    private final EnriquecerListaEspelhosService enriquecerListaEspelhosService;

    public EspelhoController(ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService,
                             ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService,
                             CriarEspelhoComItensService criarEspelhoComItensService,
                             ListarEspelhosService listarEspelhosService,
                             ListarEspelhosPorOcsService listarEspelhosPorOcsService,
                             ListarEspelhosPorStatusService listarEspelhosPorStatusService,
                             BuscarEspelhoPorIdService buscarEspelhoPorIdService,
                             ListarItensDoEspelhoService listarItensDoEspelhoService,
                             CorrigirItemEspelhoService corrigirItemEspelhoService,
                             RemoverItemDoEspelhoService removerItemDoEspelhoService, EnriquecerListaEspelhosService enriquecerListaEspelhosService) {
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
    }

    @PostMapping("/espelhos")
    public ResponseEntity<EspelhoResponse> criarEspelho(@Valid @RequestBody EspelhoRequest request) {
        GuiaProcModel atendimento = validarAtendimentoRealizadoService.validarAtendimentoRealizado(request.atendimentoId());
        if (atendimento == null) {
            throw new BusinessException(ErrorCode.ATENDIMENTO_NAO_REALIZADO);
        }

        boolean naoDuplicado = validarEspelhoNaoDuplicadoPorAtendimentoService.validarEspelhoNaoDuplicadoPorAtendimento(request.atendimentoId());
        if (!naoDuplicado) {
            throw new BusinessException(ErrorCode.ATENDIMENTO_JA_EM_ESPELHO);
        }

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
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_ESPELHO_NAO_CORRIGIDO);
        }
        return ResponseEntity.ok(EspelhoItemResponse.from(item));
    }

    @DeleteMapping("/espelhos/{id}/itens/{itemId}")
    public ResponseEntity<Void> removerItem(@PathVariable Integer id, @PathVariable Integer itemId) {
        boolean removido = removerItemDoEspelhoService.removerItemDoEspelho(id, itemId);
        if (!removido) {
            throw new BusinessException(ErrorCode.ITEM_ESPELHO_NAO_REMOVIDO);
        }
        return ResponseEntity.noContent().build();
    }

    private EspelhoModel buscarEspelhoOuFalhar(Integer id) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(id);
        if (espelho == null) {
            throw new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO);
        }
        return espelho;
    }
}
