package com.datasphere.fusex.medico.controller;

import com.datasphere.fusex.medico.dto.MedicosResponse;
import com.datasphere.fusex.medico.service.ListarMedicosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MedicoController {

    private final ListarMedicosService listarMedicosService;

    public MedicoController(ListarMedicosService listarMedicosService) {
        this.listarMedicosService = listarMedicosService;
    }

    @GetMapping("/medicos")
    public ResponseEntity<MedicosResponse> listar() {
        return ResponseEntity.ok(listarMedicosService.listar());
    }
}
