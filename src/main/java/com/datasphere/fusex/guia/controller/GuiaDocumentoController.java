package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.guia.dto.DocumentoGuia;
import com.datasphere.fusex.guia.service.BuscarDocumentoDaGuiaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GuiaDocumentoController {

    public static final String HEADER_HASH = "X-Hash-Pacote";

    private final BuscarDocumentoDaGuiaService buscarDocumentoDaGuiaService;

    public GuiaDocumentoController(BuscarDocumentoDaGuiaService buscarDocumentoDaGuiaService) {
        this.buscarDocumentoDaGuiaService = buscarDocumentoDaGuiaService;
    }

    @GetMapping("/guias/{id}/documento")
    public ResponseEntity<byte[]> documento(@PathVariable Long id) {
        DocumentoGuia documento = buscarDocumentoDaGuiaService.buscar(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=guia-" + id + ".pdf")
                .header(HEADER_HASH, documento.hash())
                .contentType(MediaType.APPLICATION_PDF)
                .body(documento.pdf());
    }
}
