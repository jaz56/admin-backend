// src/main/java/com/yassmine/administration/model/ConfigItem.java
package com.yassmine.administration.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "config_items")
public class ConfigItem {
    @Id
    private String id;
    private String category; // "fonction", "statut_candidat", "progress_step"
    private String value;
    private String label;
    private String icon;     // pour progress_step
    private String color;    // pour les badges
    private Integer  order;       // ordre d'affichage
    private Boolean active;
}