package com.datasphere.fusex.auth.controller;

import com.datasphere.fusex.auth.dto.CadastroRequest;
import com.datasphere.fusex.auth.dto.CadastroResponse;
import com.datasphere.fusex.auth.dto.LoginRequest;
import com.datasphere.fusex.auth.dto.LoginResponse;
import com.datasphere.fusex.auth.service.CadastrarBeneficiarioService;
import com.datasphere.fusex.auth.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final LoginService loginService;
    private final CadastrarBeneficiarioService cadastrarBeneficiarioService;

    public AuthController(LoginService loginService, CadastrarBeneficiarioService cadastrarBeneficiarioService) {
        this.loginService = loginService;
        this.cadastrarBeneficiarioService = cadastrarBeneficiarioService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginService.entrar(request));
    }

    @PostMapping("/auth/cadastro")
    public ResponseEntity<CadastroResponse> cadastro(@Valid @RequestBody CadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarBeneficiarioService.cadastrar(request));
    }
}
