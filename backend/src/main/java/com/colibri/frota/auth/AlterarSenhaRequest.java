package com.colibri.frota.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequest(
        @NotBlank String senhaAtual,
        @NotBlank @Size(min = 8, message = "a senha precisa ter pelo menos 8 caracteres") String novaSenha) {
}