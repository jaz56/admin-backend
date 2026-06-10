package com.yassmine.administration.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfo {
    private List<String> dossierJuridique = new ArrayList<>();
    private List<String> dossierJuridiqueOriginalNames = new ArrayList<>();
    private boolean companyVerified;
    private String companyVerificationStatus; // "pending", "verified", "rejected"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
