package com.datasphere.fusex.auth;

import java.security.Principal;
import java.util.Set;

public record UsuarioLogado(
        Integer id, String cpf, String nome, Set<Perfil> perfis, String precCp,
        Integer medicoId, String crm, Integer chefeId, Integer analistaId,
        Integer auditorId, String ocsCnpj, String cargo) implements Principal {

    public boolean tem(Perfil perfil) {
        return perfis.contains(perfil);
    }

    @Override
    public String getName() {
        return cpf;
    }
}