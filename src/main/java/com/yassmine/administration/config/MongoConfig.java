package com.yassmine.administration.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.yassmine.administration.model.embedded.NationaliteItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
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
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), "admin_db");
    }

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(
                Arrays.asList(new ArrayListToNationaliteItemConverter())
        );
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.afterPropertiesSet();

        MappingMongoConverter converter = new MappingMongoConverter(
                mongoDatabaseFactory(),
                mappingContext
        );
        converter.setCustomConversions(customConversions());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        converter.afterPropertiesSet();

        return new MongoTemplate(mongoDatabaseFactory(), converter);
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