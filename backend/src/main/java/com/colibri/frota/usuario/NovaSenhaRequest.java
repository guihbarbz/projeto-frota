package com.colibri.frota.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovaSenhaRequest(
        @NotBlank @Size(min = 8, message = "a senha precisa ter pelo menos 8 caracteres") String novaSenha) {
}