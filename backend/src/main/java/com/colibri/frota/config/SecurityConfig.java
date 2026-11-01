package com.colibri.frota.config;

import com.colibri.frota.auth.JwtAuthFilter;
import com.colibri.frota.auth.JwtService;
import com.colibri.frota.usuario.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt,
                                    UsuarioRepository usuarios) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/token-servico").hasRole("GESTOR")
                        .requestMatchers("/api/usuarios/**").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("GESTOR", "INSTRUTOR", "SERVICO")
                        .requestMatchers("/api/**").hasRole("GESTOR")
                        .anyRequest().denyAll())
                .addFilterBefore(new JwtAuthFilter(jwt, usuarios),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}