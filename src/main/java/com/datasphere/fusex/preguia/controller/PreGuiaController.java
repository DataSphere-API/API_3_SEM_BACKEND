package com.datasphere.fusex.preguia.controller;

import com.datasphere.fusex.preguia.dto.AprovacaoResponse;
import com.datasphere.fusex.preguia.dto.EmissaoResponse;
import com.datasphere.fusex.preguia.dto.PreGuiaDetalheResponse;
import com.datasphere.fusex.preguia.dto.PreGuiaResumoResponse;
import com.datasphere.fusex.preguia.dto.ReprovarRequest;
import com.datasphere.fusex.preguia.dto.ValidarPreGuiaResponse;
import com.datasphere.fusex.preguia.service.AprovarPreGuiaService;
import com.datasphere.fusex.preguia.service.DetalharPreGuiaService;
import com.datasphere.fusex.preguia.service.EmitirGuiaService;
import com.datasphere.fusex.preguia.service.ListarPreGuiasService;
import com.datasphere.fusex.preguia.service.ReprovarPreGuiaService;
import com.datasphere.fusex.preguia.service.ValidarPreGuiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PreGuiaController {

    private static final String HEADER_PREC_CP = "X-Prec-Cp";

    private final ListarPreGuiasService listarPreGuiasService;
    private final DetalharPreGuiaService detalharPreGuiaService;
    private final ValidarPreGuiaService validarPreGuiaService;
    private final AprovarPreGuiaService aprovarPreGuiaService;
    private final ReprovarPreGuiaService reprovarPreGuiaService;
    private final EmitirGuiaService emitirGuiaService;

    public PreGuiaController(ListarPreGuiasService listarPreGuiasService,
                             DetalharPreGuiaService detalharPreGuiaService,
                             ValidarPreGuiaService validarPreGuiaService,
                             AprovarPreGuiaService aprovarPreGuiaService,
                             ReprovarPreGuiaService reprovarPreGuiaService,
                             EmitirGuiaService emitirGuiaService) {
        this.listarPreGuiasService = listarPreGuiasService;
        this.detalharPreGuiaService = detalharPreGuiaService;
        this.validarPreGuiaService = validarPreGuiaService;
        this.aprovarPreGuiaService = aprovarPreGuiaService;
        this.reprovarPreGuiaService = reprovarPreGuiaService;
        this.emitirGuiaService = emitirGuiaService;
    }

    @GetMapping("/pre-guias")
    public ResponseEntity<List<PreGuiaResumoResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestHeader(value = HEADER_PREC_CP, required = false) String precCp) {
        return ResponseEntity.ok(listarPreGuiasService.listarPreGuiasPorStatus(status, precCp));
    }

    @GetMapping("/pre-guias/{id}")
    public ResponseEntity<PreGuiaDetalheResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(detalharPreGuiaService.detalhar(id));
    }

    @PostMapping("/pre-guias/{id}/validar")
    public ResponseEntity<ValidarPreGuiaResponse> validar(@PathVariable Long id) {
        return ResponseEntity.ok(validarPreGuiaService.validarPreGuia(id));
    }

    @PatchMapping("/pre-guias/{id}/aprovar")
    public ResponseEntity<AprovacaoResponse> aprovar(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_PREC_CP, required = false) String precCp) {
        return ResponseEntity.ok(aprovarPreGuiaService.aprovar(id, precCp));
    }

    @PatchMapping("/pre-guias/{id}/reprovar")
    public ResponseEntity<AprovacaoResponse> reprovar(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_PREC_CP, required = false) String precCp,
            @RequestBody(required = false) ReprovarRequest request) {
        String motivo = null;
        if (request != null) {
            motivo = request.motivo();
        }
        return ResponseEntity.ok(reprovarPreGuiaService.reprovar(id, precCp, motivo));
    }

    @PostMapping("/pre-guias/{id}/emitir")
    public ResponseEntity<EmissaoResponse> emitir(@PathVariable Long id) {
        return ResponseEntity.ok(emitirGuiaService.emitir(id));
    }
}