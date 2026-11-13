package com.datasphere.fusex.usuario.dto;

import java.util.List;

public record UsuarioResponse(Integer id, String cpf, String nome, List<String> perfis,
                               String precCp, String crm, String ocsCnpj, String cargo) {}
