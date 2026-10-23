package com.pite.Pedidosos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/pedidos/novo").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.POST, "/pedidos").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.POST, "/pedidos/*/editar", "/pedidos/*/aprovar").hasRole("ANALISTA")
                .requestMatchers(HttpMethod.POST, "/ordens/*/avancar").hasRole("ANALISTA")
                .requestMatchers(HttpMethod.POST, "/ordens/*/avancar").hasRole("ANALISTA")
                .requestMatchers(HttpMethod.POST, "/ordens/*/avancar").hasRole("ANALISTA")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"));
        return http.build();
    }
}
