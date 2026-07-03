package com.yassmine.administration.config;

import com.yassmine.administration.model.embedded.NationaliteItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.Arrays;

@Configuration
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return "admin_db";
    }

    @Override
    public com.mongodb.client.MongoClient mongoClient() {
        return com.mongodb.client.MongoClients.create(mongoUri);
    }

    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(
                Arrays.asList(new ArrayListToNationaliteItemConverter())
        );
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
            if (source == null || source.isEmpty()) return null;
            String oldCode = String.valueOf(source.get(0));
            return new NationaliteItem(oldCode, oldCode);
        }
    }
}