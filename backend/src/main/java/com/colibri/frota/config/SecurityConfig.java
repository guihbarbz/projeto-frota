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
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt,
                                    UsuarioRepository usuarios) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/token-servico").hasRole("GESTOR")
                        .requestMatchers("/api/usuarios/**").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.POST, "/api/checklists", "/api/anomalias", "/api/anomalias/*/foto")
                        .hasAnyRole("GESTOR", "INSTRUTOR")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("GESTOR", "INSTRUTOR", "SERVICO")
                        .requestMatchers("/api/**").hasRole("GESTOR")
                        .anyRequest().denyAll())
                .addFilterBefore(new JwtAuthFilter(jwt, usuarios),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();


    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${frota.cors.origens:http://localhost:4200}") List<String> origens) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(origens);
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", c);
        return fonte;
    }
}