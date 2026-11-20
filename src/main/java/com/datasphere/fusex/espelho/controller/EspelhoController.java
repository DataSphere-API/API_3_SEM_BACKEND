package com.datasphere.fusex.espelho.controller;

import com.datasphere.fusex.auditor.service.IdentificarAuditorService;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.DecisaoEspelhoResponse;
import com.datasphere.fusex.espelho.dto.EspelhoItemPatchRequest;
import com.datasphere.fusex.espelho.dto.EspelhoRequest;
import com.datasphere.fusex.espelho.dto.EspelhoResumoResponse;
import com.datasphere.fusex.espelho.dto.ReprovacaoEspelhoRequest;
import com.datasphere.fusex.espelho.service.AprovarEspelhoService;
import com.datasphere.fusex.espelho.service.BuscarEspelhoPorIdService;
import com.datasphere.fusex.espelho.service.CorrigirItemEspelhoService;
import com.datasphere.fusex.espelho.service.CriarEspelhoComItensService;
import com.datasphere.fusex.espelho.service.EnriquecerListaEspelhosService;
import com.datasphere.fusex.espelho.service.ListarEspelhosService;
import com.datasphere.fusex.espelho.service.ListarItensDoEspelhoService;
import com.datasphere.fusex.espelho.service.ReenviarEspelhoService;
import com.datasphere.fusex.espelho.service.RemoverItemDoEspelhoService;
import com.datasphere.fusex.espelho.service.ReprovarEspelhoService;
import com.datasphere.fusex.espelhoitem.dto.EspelhoItemResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EspelhoController {

    private final CriarEspelhoComItensService criarEspelhoComItensService;
    private final ListarEspelhosService listarEspelhosService;
    private final BuscarEspelhoPorIdService buscarEspelhoPorIdService;
    private final ListarItensDoEspelhoService listarItensDoEspelhoService;
    private final CorrigirItemEspelhoService corrigirItemEspelhoService;
    private final RemoverItemDoEspelhoService removerItemDoEspelhoService;
    private final EnriquecerListaEspelhosService enriquecerListaEspelhosService;
    private final AprovarEspelhoService aprovarEspelhoService;
    private final ReprovarEspelhoService reprovarEspelhoService;
    private final ReenviarEspelhoService reenviarEspelhoService;
    private final IdentificarAuditorService identificarAuditorService;

    public EspelhoController(CriarEspelhoComItensService criarEspelhoComItensService,
                             ListarEspelhosService listarEspelhosService,
                             BuscarEspelhoPorIdService buscarEspelhoPorIdService,
                             ListarItensDoEspelhoService listarItensDoEspelhoService,
                             CorrigirItemEspelhoService corrigirItemEspelhoService,
                             RemoverItemDoEspelhoService removerItemDoEspelhoService,
                             EnriquecerListaEspelhosService enriquecerListaEspelhosService,
                             AprovarEspelhoService aprovarEspelhoService,
                             ReprovarEspelhoService reprovarEspelhoService,
                             ReenviarEspelhoService reenviarEspelhoService,
                             IdentificarAuditorService identificarAuditorService) {
        this.criarEspelhoComItensService = criarEspelhoComItensService;
        this.listarEspelhosService = listarEspelhosService;
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
    public ResponseEntity<EspelhoResumoResponse> criarEspelho(@Valid @RequestBody EspelhoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(criarEspelhoComItensService.criarEspelhoComItens(request));
    }

    @GetMapping("/espelhos")
    public ResponseEntity<List<EspelhoResumoResponse>> listarEspelhos(@RequestParam(required = false) String cnpj,
                                                                      @RequestParam(required = false) StatusEspelho status) {
        return ResponseEntity.ok(listarEspelhosService.listar(cnpj, status));
    }

    @GetMapping("/espelhos/{id}")
    public ResponseEntity<EspelhoResumoResponse> buscarEspelho(@PathVariable Integer id) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoVisivel(id);
        return ResponseEntity.ok(enriquecerListaEspelhosService.enriquecer(List.of(espelho)).get(0));
    }

    @GetMapping("/espelhos/{id}/itens")
    public ResponseEntity<List<EspelhoItemResponse>> listarItens(@PathVariable Integer id) {
        buscarEspelhoPorIdService.buscarEspelhoVisivel(id);
        return ResponseEntity.ok(listarItensDoEspelhoService.listarItensDoEspelho(id));
    }

    @PatchMapping("/espelhos/{id}/itens/{itemId}")
    public ResponseEntity<Void> corrigirItem(@PathVariable Integer id,
                                             @PathVariable Integer itemId,
                                             @Valid @RequestBody EspelhoItemPatchRequest dados) {
        buscarEspelhoPorIdService.buscarEspelhoVisivel(id);
        corrigirItemEspelhoService.corrigirItemEspelho(id, itemId, dados);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/espelhos/{id}/itens/{itemId}")
    public ResponseEntity<Void> removerItem(@PathVariable Integer id, @PathVariable Integer itemId) {
        buscarEspelhoPorIdService.buscarEspelhoVisivel(id);
        removerItemDoEspelhoService.removerItemDoEspelho(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/espelhos/{id}/aprovar")
    public ResponseEntity<DecisaoEspelhoResponse> aprovarEspelho(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auditor-Id", required = false) Integer headerAuditorId) {
        buscarEspelhoPorIdService.buscarEspelhoVisivel(id);
        Integer auditorId = identificarAuditorService.identificarAuditor(headerAuditorId);
        return ResponseEntity.ok(aprovarEspelhoService.aprovarEspelho(id, auditorId));
    }

    @PatchMapping("/espelhos/{id}/reprovar")
    public ResponseEntity<DecisaoEspelhoResponse> reprovarEspelho(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auditor-Id", required = false) Integer headerAuditorId,
            @RequestBody(required = false) ReprovacaoEspelhoRequest request) {
        buscarEspelhoPorIdService.buscarEspelhoVisivel(id);
        Integer auditorId = identificarAuditorService.identificarAuditor(headerAuditorId);
        String motivo = request != null ? request.motivo() : null;
        return ResponseEntity.ok(reprovarEspelhoService.reprovarEspelho(id, auditorId, motivo));
    }

    @PostMapping("/espelhos/{id}/reenviar")
    public ResponseEntity<DecisaoEspelhoResponse> reenviarEspelho(@PathVariable Integer id) {
        buscarEspelhoPorIdService.buscarEspelhoVisivel(id);
        return ResponseEntity.ok(reenviarEspelhoService.reenviarEspelho(id));
    }
}
