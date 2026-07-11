package com.yassmine.administration.dto.request;

import com.yassmine.administration.model.embedded.NationaliteItem;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CreateUserRequest {
    private String email;
    private String nom;
    private String prenom;
    private String password;
    private String role;
    private String sexe;
    private String dateDeNaissance;
    private String numeroTel;
    private PaysRequest pays;         // objet { value, label }
    private String address;
    private String codePostal;
    private NationaliteItem nationalite;
    private String dernierePosteOccupe;
    private String fonction;
    private String langueDeProcedure;
    private String pieceIdentite;

    @Data
    public static class PaysRequest {
        private String value;
        private String label;
    }
}