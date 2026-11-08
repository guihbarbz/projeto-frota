package com.colibri.frota.auth;

import com.colibri.frota.config.UsuariosIniciais;
import com.colibri.frota.usuario.Perfil;
import com.colibri.frota.usuario.Usuario;
import com.colibri.frota.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UsuarioRepository repo, PasswordEncoder encoder, JwtService jwt) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        Usuario u = repo.findByEmail(req.email().trim().toLowerCase())
                .filter(Usuario::isAtivo)
                .filter(x -> x.getPerfil() != Perfil.SERVICO)
                .filter(x -> encoder.matches(req.senha(), x.getSenhaHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Credenciais inválidas"));
        return jwt.gerar(u);
    }

    /** Cada usuário troca a própria senha, informando a atual. */
    @PutMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@Valid @RequestBody AlterarSenhaRequest req, Authentication auth) {
        Usuario u = repo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        // 400 (e não 401) de propósito: o 401 faria o aplicativo deslogar a pessoa
        if (!encoder.matches(req.senhaAtual(), u.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
        }
        u.setSenhaHash(encoder.encode(req.novaSenha()));
        repo.save(u);
    }

    /** Só o gestor gera o token da automação (n8n). Trate-o como uma senha. */
    @PostMapping("/token-servico")
    public TokenResponse tokenServico() {
        Usuario servico = repo.findByEmail(UsuariosIniciais.EMAIL_SERVICO)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Usuário de serviço não encontrado"));
        return jwt.gerar(servico);
    }
}