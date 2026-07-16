package com.yassmine.administration.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yassmine.administration.model.PermissionDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String userId;
    private String email;
    private String role;
    private String nom;
    private String prenom;
    private String photoDeProfile;
    private Double balance;
    private String candidateVerificationStatus; // Permet d'adapter l'UI Angular directement

    // 🟢 NOUVEAU : nécessaire pour PermissionGuard côté Angular
    // @JsonProperty force la clé JSON à "isSuperAdmin" (Jackson tend à la
    // transformer en "superAdmin" en retirant le préfixe "is" sur un booléen sinon)
    @JsonProperty("isSuperAdmin")
    private boolean isSuperAdmin;
    private Map<String, PermissionDetail> permissions;
    private LocalDate permissionsExpiryDate;
    private boolean isBlocked;
    private LocalDate blockedUntil;
}