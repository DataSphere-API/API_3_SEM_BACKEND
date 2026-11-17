package com.datasphere.fusex.espelho.controller;

import com.datasphere.fusex.auditor.service.IdentificarAuditorService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.*;
import com.datasphere.fusex.espelho.request.DecisaoEspelhoResponse;
import com.datasphere.fusex.espelho.request.ReprovacaoEspelhoRequest;
import com.datasphere.fusex.espelho.service.*;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.dto.EspelhoItemResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
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
    private final AprovarEspelhoService aprovarEspelhoService;
    private final ReprovarEspelhoService reprovarEspelhoService;
    private final ReenviarEspelhoService reenviarEspelhoService;
    private final IdentificarAuditorService identificarAuditorService;

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
                             AprovarEspelhoService aprovarEspelhoService,
                             ReprovarEspelhoService reprovarEspelhoService,
                             ReenviarEspelhoService reenviarEspelhoService,
                             IdentificarAuditorService identificarAuditorService) {
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
        this.aprovarEspelhoService = aprovarEspelhoService;
        this.reprovarEspelhoService = reprovarEspelhoService;
        this.reenviarEspelhoService = reenviarEspelhoService;
        this.identificarAuditorService = identificarAuditorService;
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
    public ResponseEntity<DecisaoEspelhoResponse> aprovarEspelho(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auditor-Id", required = false) Integer headerAuditorId) {
        buscarEspelhoOuFalhar(id);
        Integer auditorId = identificarAuditorService.identificarAuditor(headerAuditorId);
        DecisaoEspelhoResponse response = aprovarEspelhoService.aprovarEspelho(id, auditorId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/espelhos/{id}/reprovar")
    public ResponseEntity<DecisaoEspelhoResponse> reprovarEspelho(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auditor-Id", required = false) Integer headerAuditorId,
            @RequestBody(required = false) ReprovacaoEspelhoRequest request) {
        buscarEspelhoOuFalhar(id);
        Integer auditorId = identificarAuditorService.identificarAuditor(headerAuditorId);
        String motivo = request != null ? request.motivo() : null;
        DecisaoEspelhoResponse response = reprovarEspelhoService.reprovarEspelho(id, auditorId, motivo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/espelhos/{id}/reenviar")
    public ResponseEntity<DecisaoEspelhoResponse> reenviarEspelho(@PathVariable Integer id) {
        buscarEspelhoOuFalhar(id);
        DecisaoEspelhoResponse response = reenviarEspelhoService.reenviarEspelho(id);
        return ResponseEntity.ok(response);
    }
}
