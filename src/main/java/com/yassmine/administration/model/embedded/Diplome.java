package com.yassmine.administration.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat; // <-- Ne pas oublier l'import
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diplome {
    @Field("_id") // Plus robuste pour un document imbriqué qu'un simple @Id
    private String id;
    private String titre;
    private String date;
    private String etablissement;
}
