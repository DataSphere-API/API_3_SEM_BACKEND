package com.datasphere.fusex.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Integer> {
    boolean existsByLogin(String login);
}
