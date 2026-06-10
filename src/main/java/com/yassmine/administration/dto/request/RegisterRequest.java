package com.yassmine.administration.dto.request;

import com.yassmine.administration.model.embedded.Pays;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format de l'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le rôle est obligatoire")
    private String role; // "candidat", "admin", "company"

    @NotNull(message = "Les conditions d'utilisation doivent être acceptées")
    private Boolean acceptedTerms;

    // Optionnels lors de la première étape de l'inscription
    private String numeroTel;
    private Pays pays;
    private List<String> interessesPar;
    private String sexe;
}