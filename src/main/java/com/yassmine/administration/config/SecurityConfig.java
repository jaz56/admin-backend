package com.yassmine.administration.config;

import com.yassmine.administration.security.JwtAuthEntryPoint;
import com.yassmine.administration.security.JwtAuthFilter;
import com.yassmine.administration.security.AdminPermissionFilter; // 🟢 IMPORT DU NOUVEAU FILTRE
import com.yassmine.administration.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AdminPermissionFilter adminPermissionFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    // 🟢 NOUVEAU : liste des origines autorisées, configurable via variable d'environnement
    // CORS_ALLOWED_ORIGINS (séparées par des virgules), avec un défaut sûr pour le dev local.
    // Ça permet à la même image Docker de fonctionner sur dev/staging/prod sans jamais
    // recompiler — juste en changeant la variable d'environnement au déploiement.
    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOriginsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Routes admin
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "admin", "ROLE_admin")

                        // Routes candidat
                        .requestMatchers("/api/users/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "admin", "ROLE_admin", "CANDIDAT", "ROLE_CANDIDAT", "candidat", "ROLE_candidat")
                        .requestMatchers("/api/bookings/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "admin", "ROLE_admin", "CANDIDAT", "ROLE_CANDIDAT", "candidat", "ROLE_candidat")
                        .requestMatchers("/api/demandes/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "admin", "ROLE_admin", "CANDIDAT", "ROLE_CANDIDAT", "candidat", "ROLE_candidat")
                        .requestMatchers("/api/orders/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "admin", "ROLE_admin", "CANDIDAT", "ROLE_CANDIDAT", "candidat", "ROLE_candidat")
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/countries/active").permitAll()
                        .requestMatchers("/api/config/**").permitAll()
                        .requestMatchers("/api/roles/**").permitAll()

                        // Tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(adminPermissionFilter, JwtAuthFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 🟢 CORRIGÉ : découpe la liste d'origines fournie par variable d'environnement
        // (séparées par des virgules) au lieu d'une seule valeur figée en dur.
        List<String> allowedOrigins = Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        config.setAllowedOrigins(allowedOrigins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        config.setAllowedHeaders(List.of("Origin", "Access-Control-Allow-Origin", "Content-Type",
                "Accept", "Authorization", "Origin, Accept", "X-Requested-With",
                "Access-Control-Request-Method", "Access-Control-Request-Headers"));

        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}