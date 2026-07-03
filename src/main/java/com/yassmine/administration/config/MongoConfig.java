package com.yassmine.administration.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.yassmine.administration.model.embedded.NationaliteItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.Arrays;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(
                Arrays.asList(new ArrayListToNationaliteItemConverter())
        );
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        // 1. Créer le mapping context
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.afterPropertiesSet();

        // 2. Créer le converter avec les CustomConversions injectées
        MappingMongoConverter converter = new MappingMongoConverter(
                org.springframework.data.mongodb.core.convert.NoOpDbRefResolver.INSTANCE,
                mappingContext
        );
        converter.setCustomConversions(customConversions()); // ← clé du fix
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        converter.afterPropertiesSet();

        // 3. Créer le MongoTemplate avec ce converter
        return new MongoTemplate(mongoClient(), "admin_db", converter);
    }

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(
            LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }

    @ReadingConverter
    public static class ArrayListToNationaliteItemConverter
            implements Converter<ArrayList, NationaliteItem> {

        @Override
        public NationaliteItem convert(ArrayList source) {
            if (source == null || source.isEmpty()) {
                return null;
            }
            String oldCode = String.valueOf(source.get(0));
            return new NationaliteItem(oldCode, oldCode);
        }
    }
}