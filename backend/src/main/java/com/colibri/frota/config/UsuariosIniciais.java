package com.colibri.frota.config;

import com.colibri.frota.usuario.Perfil;
import com.colibri.frota.usuario.Usuario;
import com.colibri.frota.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UsuariosIniciais implements CommandLineRunner {

    public static final String EMAIL_SERVICO = "servico@frota.local";
    private static final Logger log = LoggerFactory.getLogger(UsuariosIniciais.class);

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminSenha;

    public UsuariosIniciais(UsuarioRepository repo, PasswordEncoder encoder,
                            @Value("${frota.admin.email}") String adminEmail,
                            @Value("${frota.admin.senha}") String adminSenha) {
        this.repo = repo;
        this.encoder = encoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminSenha = adminSenha;
    }

    @Override
    public void run(String... args) {
        // usuário da automação: senha aleatória que ninguém usa (ele só entra por token)
        if (!repo.existsByEmail(EMAIL_SERVICO)) {
            criar("Automação (somente leitura)", EMAIL_SERVICO, UUID.randomUUID().toString(), Perfil.SERVICO);
        }
        if (!repo.existsByEmail(adminEmail)) {
            if (adminSenha.length() < 8) {
                log.warn("ADMIN_SENHA não definida (mínimo 8 caracteres): gestor inicial NÃO foi criado.");
            } else {
                criar("Gestor", adminEmail, adminSenha, Perfil.GESTOR);
                log.info("Gestor inicial criado: {}", adminEmail);
            }
        }
    }

    private void criar(String nome, String email, String senha, Perfil perfil) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenhaHash(encoder.encode(senha));
        u.setPerfil(perfil);
        repo.save(u);
    }
}