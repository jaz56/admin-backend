package com.yassmine.administration.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubAdminRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    // null = accès à durée indéfinie
    private LocalDate permissionsExpiryDate;

    // Ex: { "users": { "read": true, "create": false, "update": true }, ... }
    private Map<String, Map<String, Boolean>> permissions;
}