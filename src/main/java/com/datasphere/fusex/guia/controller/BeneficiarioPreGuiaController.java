package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.guia.dto.PreGuiaDoBeneficiarioResponse;
import com.datasphere.fusex.guia.service.ListarPreGuiasDoBeneficiarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BeneficiarioPreGuiaController {

    private final ListarPreGuiasDoBeneficiarioService listarPreGuiasDoBeneficiarioService;

    public BeneficiarioPreGuiaController(ListarPreGuiasDoBeneficiarioService listarPreGuiasDoBeneficiarioService) {
        this.listarPreGuiasDoBeneficiarioService = listarPreGuiasDoBeneficiarioService;
    }

    @GetMapping("/beneficiarios/{precCp}/pre-guias")
    public List<PreGuiaDoBeneficiarioResponse> listar(@PathVariable String precCp) {
        return listarPreGuiasDoBeneficiarioService.listar(precCp);
    }
}
