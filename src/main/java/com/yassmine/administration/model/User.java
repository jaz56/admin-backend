package com.yassmine.administration.model;

import com.yassmine.administration.model.embedded.CompanyInfo;
import com.yassmine.administration.model.embedded.Pays;
import com.yassmine.administration.model.embedded.StbInfo;
import com.yassmine.administration.model.embedded.TransactionDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    private String email;
    private String uniqueId;
    private String nom;
    private String prenom;
    private String password;

    private boolean acceptedTerms;
    private boolean nvRegister;
    private String progress;
    private boolean verifiedEmail;
    private String role;

    private LocalDateTime dateDeNaissance;
    private String sexe;

    private Pays pays;

    private String numeroTel;
    private List<String> interessesPar = new ArrayList<>();

    private String photoDeProfile;

    private StbInfo stbInfo;
    private CompanyInfo companyInfo;
    private BigDecimal balance;
    private BigDecimal cashback;

    // Listes
    private List<String> favoriteJobs = new ArrayList<>();
    private List<String> nationalite = new ArrayList<>();
    private List<String> subscriptions = new ArrayList<>();
    private List<String> providers = new ArrayList<>();

    // Transactions (à typer plus précisément si vous avez le modèle)

    private List<TransactionDetail> balanceTransactions = new ArrayList<>();
    private List<TransactionDetail> cashbackTransactions = new ArrayList<>();
    // Email reminder
    private LocalDateTime complettProfileEmailLastSent;
    private Integer complettProfileEmailReminderCount;

    // Vérification candidat
    private boolean candidateVerified;
    private String candidateVerificationStatus;
    private Integer profileCompletionPercentage;
    private LocalDateTime lastProfileUpload;

    // Profil complémentaire
    private String address;
    private String codePostal;
    private String pieceIdentite;

    public String getDernierePosteOccupe() {
        return dernierePosteOccupe;
    }

    public void setDernierePosteOccupe(String dernierePosteOccupe) {
        this.dernierePosteOccupe = dernierePosteOccupe;
    }

    private String dernierePosteOccupe;
    private String fonction;
    private String langueDeProcedure;

    // Documents identité
    private String cinRecto;
    private String cinVerso;
    private String vocal;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
