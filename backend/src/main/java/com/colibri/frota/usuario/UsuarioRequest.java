package com.colibri.frota.usuario;

import jakarta.validation.constraints.*;

public record UsuarioRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "a senha precisa ter pelo menos 8 caracteres") String senha,
        @NotNull Perfil perfil) {
}