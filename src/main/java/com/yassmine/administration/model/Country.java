package com.yassmine.administration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "countries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Country {

    @Id
    private String id;

    @Field("id") // CORRECTION : Indique à Spring de mapper la clé "id" numérique du JSON
    private Integer countryId;
    private String code;
    private String name;

    @Field("region")
    private Region region;

    private Integer jobCount;
    private LocalDateTime lastSynced;
    private boolean isActive;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Region {
        private Integer id;
        private String name;
    }
}
