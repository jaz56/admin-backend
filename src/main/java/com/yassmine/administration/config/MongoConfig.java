package com.yassmine.administration.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableMongoAuditing // ACTIVE LE REMPLISSAGE AUTOMATIQUE DES DATES (createdAt, updatedAt)
public class MongoConfig {

    // On injecte l'URI de configuration de manière dynamique
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    /**
     * Utilise l'URI dynamique (s'adapte automatiquement à Docker ou Local)
     */
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    /**
     * Utilise la connexion dynamique pour manipuler la base admin_db
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "admin_db");
    }

    /**
     * Ce Bean permet d'activer la validation des contraintes (comme @NotBlank, @Email)
     * directement au niveau des documents MongoDB avant qu'ils ne soient enregistrés.
     */
    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }
}