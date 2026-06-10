package com.yassmine.administration.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nom;
    private String prenom;
    private String email;
    private String numeroTel;
    private String address;
    private String codePostal;
    private String fonction;
    private String langueDeProcedure;
}