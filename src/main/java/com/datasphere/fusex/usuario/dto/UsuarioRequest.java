package com.datasphere.fusex.usuario.dto;

import java.util.List;

public record UsuarioRequest(String tipo, String precCp, List<String> perfis, String crm,
                              String cpf, String senha, String ocsCnpj, String cargo) {}
