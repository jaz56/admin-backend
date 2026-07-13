package com.yassmine.administration.dto.response;

import com.yassmine.administration.model.embedded.Pays;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeResponseDTO {
    private String id;
    private String uniqueId;
    private String type;
    private String status;
    private String journeesDestination;
    private String niveauEtude;
    private LocalDateTime createdAt;

    // Les informations du candidat récupérées depuis la collection Users
    private String candidatNom;
    private String candidatPrenom;
    private String candidatEmail;    // ← ajouter
    private String candidatTel;      // ← ajouter
    private String candidatUniqueId;
    private String progress;
    private Pays paysResidence;
    private String fonction;// ← ajouter
}