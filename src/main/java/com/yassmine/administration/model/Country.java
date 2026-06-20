package com.yassmine.administration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "countries")
public class Country {

    @Id
    private String id;

    @Field("id")
    private Integer countryId;

    private String code;
    private String name;

    @Field("region")
    private Region region;

    @Field("job_count")
    private Integer jobCount;

    @Field("last_synced")
    private LocalDateTime lastSynced;

    // ← boolean primitif avec @Field et @JsonProperty explicites
    @Field("is_active")
    @JsonProperty("isActive")
    private boolean active;  // ← "active" et non "isActive" pour éviter le conflit Lombok

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Region {
        private Integer id;
        private String name;
    }
}