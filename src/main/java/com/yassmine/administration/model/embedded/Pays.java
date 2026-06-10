package com.yassmine.administration.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pays {
    @Field("_id") //  Permet de récupérer le _id présent dans le JSON imbriqué
    private String id;
    private String value;
    private String label;
}