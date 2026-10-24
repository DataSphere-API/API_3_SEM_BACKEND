package com.datasphere.fusex.preguia;

import com.datasphere.fusex.preguia.DTO.AprovacaoResponse;
import com.datasphere.fusex.preguia.DTO.ReprovarRequest;
import com.datasphere.fusex.preguia.service.AtualizarStatusPreGuiaService;
import com.datasphere.fusex.preguia.service.BuscarPreGuiaPorIdService;
import com.datasphere.fusex.preguia.service.ValidarPreGuiaValidadaService;
import com.datasphere.fusex.preguia.service.VincularChefeFusexService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController("preGuiaAprovacaoController")
public class PreGuiaController {

    private static final String HEADER_CHEFE_ID = "X-Chefe-Id";

    private final BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    private final ValidarPreGuiaValidadaService validarPreGuiaValidadaService;
    private final VincularChefeFusexService vincularChefeFusexService;
    private final AtualizarStatusPreGuiaService atualizarStatusPreGuiaService;

    public PreGuiaController(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                             ValidarPreGuiaValidadaService validarPreGuiaValidadaService,
                             VincularChefeFusexService vincularChefeFusexService,
                             AtualizarStatusPreGuiaService atualizarStatusPreGuiaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.validarPreGuiaValidadaService = validarPreGuiaValidadaService;
        this.vincularChefeFusexService = vincularChefeFusexService;
        this.atualizarStatusPreGuiaService = atualizarStatusPreGuiaService;
    }

    @PatchMapping("/pre-guias/{id}/aprovar")
    public ResponseEntity<?> aprovar(@PathVariable Long id,
                                     @RequestHeader(HEADER_CHEFE_ID) Long chefeId) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaPorId(id);

        if (preGuia == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PRE_GUIA_NAO_ENCONTRADA");
        }

        if (!validarPreGuiaValidadaService.validarPreGuiaValidada(preGuia)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("PRE_GUIA_NAO_VALIDADA");
        }

        if (!vincularChefeFusexService.vincularChefeFusex(preGuia, chefeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CHEFE_NAO_ENCONTRADO");
        }

        AprovacaoResponse resposta =
                atualizarStatusPreGuiaService.atualizarStatusPreGuia(preGuia, StatusPreGuia.APROVADA, null);
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/pre-guias/{id}/reprovar")
    public ResponseEntity<?> reprovar(@PathVariable Long id,
                                      @RequestHeader(HEADER_CHEFE_ID) Long chefeId,
                                      @RequestBody(required = false) ReprovarRequest request) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaPorId(id);

        if (preGuia == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PRE_GUIA_NAO_ENCONTRADA");
        }

        if (request == null || request.motivo() == null || request.motivo().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MOTIVO_OBRIGATORIO");
        }

        if (!validarPreGuiaValidadaService.validarPreGuiaValidada(preGuia)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("PRE_GUIA_NAO_VALIDADA");
        }

        if (!vincularChefeFusexService.vincularChefeFusex(preGuia, chefeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CHEFE_NAO_ENCONTRADO");
        }

        AprovacaoResponse resposta = atualizarStatusPreGuiaService.atualizarStatusPreGuia(
                preGuia, StatusPreGuia.REPROVADA, request.motivo().trim());
        return ResponseEntity.ok(resposta);
    }
}
