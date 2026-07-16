package com.yassmine.administration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yassmine.administration.model.embedded.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    // ── Compte ─────────────────────────────────────────────────────
    private String email;
    private String secondEmail;          // ← NOUVEAU
    private String uniqueId;
    private String nom;
    private String prenom;
    private String password;

    private boolean acceptedTerms;
    private boolean nvRegister;
    private String  progress;
    private boolean verifiedEmail;
    private String  role;

    // ── Infos personnelles ─────────────────────────────────────────
    private LocalDateTime dateDeNaissance;
    private String sexe;
    private String lieuDeNaissance;      // ← NOUVEAU
    private String numeroTel;
    private String address;
    private String codePostal;
    private String pieceIdentite;

    // ── Pays ───────────────────────────────────────────────────────
    private Pays pays;

    // ── Nationalité — maintenant liste d'objets ────────────────────
    private NationaliteItem nationalite;  // ← CHANGÉ

    // ── Médias ─────────────────────────────────────────────────────
    private String photoDeProfile;
    private String cinRecto;
    private String cinVerso;
    private String vocal;

    // ── Profil professionnel ───────────────────────────────────────
    private String dernierePosteOccupe;
    private String fonction;
    private String langueDeProcedure;

    @Builder.Default
    private List<String> interessesPar = new ArrayList<>();

    @Builder.Default
    private List<String> favoriteJobs = new ArrayList<>();

    // ── Statut candidat ────────────────────────────────────────────
    private boolean candidateVerified;
    private String  candidateVerificationStatus;
    private Integer profileCompletionPercentage;

    // ── Finances ───────────────────────────────────────────────────
    private Double balance;
    private Double cashback;

    @Builder.Default
    private List<TransactionDetail> balanceTransactions  = new ArrayList<>();

    @Builder.Default
    private List<TransactionDetail> cashbackTransactions = new ArrayList<>();

    // ── Abonnements — maintenant liste d'objets ────────────────────
    @Builder.Default
    private List<Subscription> subscriptions = new ArrayList<>();   // ← CHANGÉ

    @Builder.Default
    private List<String> providers = new ArrayList<>();

    // ── STB ────────────────────────────────────────────────────────
    private StbInfo stbInfo;
    private LocalDateTime stbProfileUpdatedAt;                      // ← NOUVEAU

    // ── Entreprise ─────────────────────────────────────────────────
    private CompanyInfo companyInfo;

    // ── Email reminder ─────────────────────────────────────────────
    private LocalDateTime complettProfileEmailLastSent;
    private Integer       complettProfileEmailReminderCount;
    private LocalDateTime lastProfileUpload;

    private Object isSuperAdmin;

    @Builder.Default
    private Map<String, PermissionDetail> permissions = new HashMap<>();

    private LocalDate permissionsExpiryDate;

    // 🟢 CORRIGÉ : @JsonProperty force la clé JSON à "isBlocked".
    // Sans ça, Jackson retire le préfixe "is" du getter généré par Lombok
    // (isBlocked() -> propriété "blocked"), et le frontend qui teste
    // "admin.isBlocked" reçoit toujours undefined.
    @JsonProperty("isBlocked")
    private boolean isBlocked;

    private LocalDate blockedUntil;

    @CreatedDate
    private LocalDateTime createdAt;
    // ── Timestamps ─────────────────────────────────────────────────

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ── Getter/Setter manuel (gardé pour compatibilité) ────────────
    public String getDernierePosteOccupe() { return dernierePosteOccupe; }
    public void setDernierePosteOccupe(String v) { this.dernierePosteOccupe = v; }

    public boolean isSuperAdmin() {
        if (this.isSuperAdmin instanceof Boolean) {
            return (Boolean) this.isSuperAdmin;
        }
        if (this.isSuperAdmin instanceof String) {
            return Boolean.parseBoolean((String) this.isSuperAdmin);
        }
        return false;
    }
}