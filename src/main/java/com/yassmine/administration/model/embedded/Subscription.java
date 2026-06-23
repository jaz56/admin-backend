package com.yassmine.administration.model.embedded;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subscription {
    @Field("_id")
    private String id;
    private String subscriptionType;
    private LocalDateTime endsAt;
    private Integer remainingAccesses;
    private LocalDateTime purchasedAt;
}