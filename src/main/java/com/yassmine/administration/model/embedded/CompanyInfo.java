package com.yassmine.administration.model.embedded;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class CompanyInfo {
    private List<String> dossierJuridique = new ArrayList<>();
    private List<String> dossierJuridiqueOriginalNames = new ArrayList<>();
    private boolean companyVerified;
    private String companyVerificationStatus; // "pending", "verified", "rejected"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
