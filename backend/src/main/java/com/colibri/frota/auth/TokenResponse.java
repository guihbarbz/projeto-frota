package com.colibri.frota.auth;

import com.colibri.frota.usuario.Perfil;
import java.time.Instant;

public record TokenResponse(String token, String tipo, Perfil perfil, String nome, Instant expiraEm) {
}