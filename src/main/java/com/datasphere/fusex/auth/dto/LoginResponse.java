package com.datasphere.fusex.auth.dto;

public record LoginResponse(String token, UsuarioResponse usuario) {}