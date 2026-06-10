package com.yassmine.administration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yassmine.administration.model.embedded.ConnaissanceLinguistique;
import com.yassmine.administration.model.embedded.Diplome;
import com.yassmine.administration.model.embedded.Pays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "demandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Demande {

    @Id
    private String id;

    @Field("user")
    private String userId;

    private String uniqueId;
    private String type;          // "travail", "etude"...
    private String status;        // "pre_selection", ...
    private String progress;
    private String eligibiliteNote;
    private String journeesDestination;

    private Pays paysResidence;

    private boolean ouverteTouteOpportunites;
    private boolean besoinVisa;
    private boolean typeHebergement;
    private boolean maladieContagieuse;
    private boolean handicape;
    private boolean preinscription;
    private boolean existenceDeGarant;

    private String niveauEtude;
    private Integer nombreAnneesEtude;
    private Integer nombreMoisStage;
    private String activite;
    private String condidatStatutActuel;
    private String dernierPosteOccupe;
    private String fonction;
    private String nombreAnneesExperience;
    private String posteSouhaite;

    private List<Diplome> diplomes = new ArrayList<>();
    private List<ConnaissanceLinguistique> connaissanceLinguistique = new ArrayList<>();
    private List<String> anneesDeEtudeAFinancier = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}