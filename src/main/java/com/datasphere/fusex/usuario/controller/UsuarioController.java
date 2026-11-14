package com.datasphere.fusex.usuario.controller;

import com.datasphere.fusex.usuario.dto.UsuarioRequest;
import com.datasphere.fusex.usuario.dto.UsuarioResponse;
import com.datasphere.fusex.usuario.dto.UsuariosResponse;
import com.datasphere.fusex.usuario.service.CadastrarUsuarioService;
import com.datasphere.fusex.usuario.service.ListarUsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
public class UsuarioController {

    private final CadastrarUsuarioService cadastrarUsuarioService;
    private final ListarUsuariosService listarUsuariosService;

    public UsuarioController(CadastrarUsuarioService cadastrarUsuarioService, ListarUsuariosService listarUsuariosService) {
        this.cadastrarUsuarioService = cadastrarUsuarioService;
        this.listarUsuariosService = listarUsuariosService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<UsuariosResponse> listar(Principal principal) {
        return ResponseEntity.ok(listarUsuariosService.listar(principal));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> cadastrar(@RequestBody UsuarioRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarUsuarioService.cadastrar(request, principal));
    }
}
