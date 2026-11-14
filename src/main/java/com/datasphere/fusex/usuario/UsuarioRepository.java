package com.datasphere.fusex.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Integer> {

    boolean existsByLogin(String login);

    Optional<UsuarioModel> findByLogin(String login);
}