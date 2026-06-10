package com.yassmine.administration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // S'applique à tous les Endpoints de l'API
                .allowedOrigins("http://localhost:4200") // Autorise votre application Angular
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Méthodes HTTP autorisées
                .allowedHeaders("*") // Autorise tous les headers (Authorization, Content-Type, etc.)
                .allowCredentials(true) // Obligatoire pour envoyer les cookies ou sessions si nécessaire
                .maxAge(3600); // Cache la réponse de pré-vol (Pre-flight OPTIONS) pendant 1 heure
    }
}