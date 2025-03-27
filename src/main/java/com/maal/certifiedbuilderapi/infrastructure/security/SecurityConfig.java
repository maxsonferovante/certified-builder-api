package com.maal.certifiedbuilderapi.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ApiKeyAuthFilter apiKeyAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Desabilita a proteção CSRF
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class) // Adiciona o filtro customizado
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated() // Todas as requisições precisam estar autenticadas
                )
                .httpBasic(withDefaults()); // Exemplo com HTTP Basic, se necessário

        return http.build();
    }
}
