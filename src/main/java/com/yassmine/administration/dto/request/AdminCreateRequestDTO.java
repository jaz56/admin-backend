package com.yassmine.administration.dto.request;

import com.yassmine.administration.model.PermissionDetail;
import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
public class AdminCreateRequestDTO {
    private String nom;
    private String prenom;
    private String email;
    private Map<String, PermissionDetail> permissions;
    private LocalDate permissionsExpiryDate;
}