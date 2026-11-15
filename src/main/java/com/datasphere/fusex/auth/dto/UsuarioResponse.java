package com.datasphere.fusex.auth.dto;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;

import java.util.ArrayList;
import java.util.List;

public record UsuarioResponse(
        Integer id,
        String nome,
        String cpf,
        List<String> perfis,
        String precCp,
        String crm,
        Integer medicoId,
        String ocsCnpj,
        String cargo) {

    public static UsuarioResponse de(UsuarioLogado usuario) {
        List<String> perfis = new ArrayList<>();
        for (Perfil perfil : Perfil.values()) {
            if (usuario.tem(perfil)) {
                perfis.add(perfil.name());
            }
        }
        return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.cpf(), perfis, usuario.precCp(),
                usuario.crm(), usuario.medicoId(), usuario.ocsCnpj(), usuario.cargo());
    }
}