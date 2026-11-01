package com.colibri.frota.auth;

import com.colibri.frota.usuario.Perfil;
import com.colibri.frota.usuario.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long horas;
    private final long diasServico;

    public JwtService(@Value("${frota.jwt.secret}") String segredo,
                      @Value("${frota.jwt.expiracao-horas}") long horas,
                      @Value("${frota.jwt.expiracao-servico-dias}") long diasServico) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.horas = horas;
        this.diasServico = diasServico;
    }

    public TokenResponse gerar(Usuario u) {
        Instant agora = Instant.now();
        Instant expira = (u.getPerfil() == Perfil.SERVICO)
                ? agora.plus(diasServico, ChronoUnit.DAYS)
                : agora.plus(horas, ChronoUnit.HOURS);

        String token = Jwts.builder()
                .subject(u.getEmail())
                .claim("perfil", u.getPerfil().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expira))
                .signWith(chave)
                .compact();

        return new TokenResponse(token, "Bearer", u.getPerfil(), u.getNome(), expira);
    }

    /** Valida assinatura e validade; lança JwtException se o token não servir. */
    public String emailDoToken(String token) {
        return Jwts.parser().verifyWith(chave).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }
}