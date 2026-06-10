package com.yassmine.administration.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private BigDecimal balance;
    private String candidateVerificationStatus; // Permet d'adapter l'UI Angular directement
}
