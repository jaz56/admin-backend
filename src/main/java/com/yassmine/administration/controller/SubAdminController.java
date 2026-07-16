package com.yassmine.administration.controller;

import com.yassmine.administration.dto.request.CreateSubAdminRequest;
import com.yassmine.administration.model.PermissionDetail;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.UserRepository;
import com.yassmine.administration.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sub-admins")
@RequiredArgsConstructor
public class SubAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 0. NOUVEAU : Créer un sous-administrateur
     * Génère un mot de passe aléatoire, l'encode, crée l'utilisateur avec ses
     * permissions et envoie un email de bienvenue avec les accès.
     */
    @PostMapping
    public ResponseEntity<?> createSubAdmin(@Valid @RequestBody CreateSubAdminRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cet email est déjà utilisé."));
        }

        // 1. Génération du mot de passe temporaire (12 caractères)
        String rawPassword = generateRandomPassword(12);

        // 2. Construction de la map de permissions à partir du DTO
        Map<String, PermissionDetail> permissions = request.getPermissions() == null
                ? Map.of()
                : request.getPermissions().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> PermissionDetail.builder()
                                .read(e.getValue().getOrDefault("read", false))
                                .create(e.getValue().getOrDefault("create", false))
                                .update(e.getValue().getOrDefault("update", false))
                                .build()
                ));

        // 3. Création de l'utilisateur admin
        User admin = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role("admin")
                .isSuperAdmin(false)
                .acceptedTerms(true)
                .verifiedEmail(false)
                .permissions(permissions)
                .permissionsExpiryDate(request.getPermissionsExpiryDate())
                .isBlocked(false)
                .balance(0.0)
                .cashback(0.0)
                .candidateVerificationStatus("pending")
                .build();

        userRepository.save(admin);

        // 4. Envoi de l'email de bienvenue avec identifiants + permissions
        emailService.sendAdminWelcomeEmail(admin, rawPassword);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Administrateur créé avec succès. Email envoyé."));
    }

    /**
     * 1. Lister tous les sous-administrateurs (exclut le Super Admin et les candidats)
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllSubAdmins() {
        List<User> admins = userRepository.findByRoleAndIsSuperAdminFalse("admin");
        return ResponseEntity.ok(admins);
    }

    /**
     * 2. Modifier les permissions d'un sous-admin
     */
    @PutMapping("/{id}/permissions")
    public ResponseEntity<?> updatePermissions(
            @PathVariable String id,
            @RequestBody Map<String, Map<String, Boolean>> request) {

        return userRepository.findById(id).map(admin -> {
            if (admin.isSuperAdmin()) {
                return ResponseEntity.badRequest().body("Interdit de modifier les droits du Super Admin.");
            }

            Map<String, PermissionDetail> currentPermissions = admin.getPermissions();

            request.forEach((module, actions) -> {
                PermissionDetail detail = PermissionDetail.builder()
                        .read(actions.getOrDefault("read", false))
                        .create(actions.getOrDefault("create", false))
                        .update(actions.getOrDefault("update", false))
                        .build();
                currentPermissions.put(module, detail);
            });

            admin.setPermissions(currentPermissions);
            userRepository.save(admin);
            return ResponseEntity.ok().body(Map.of("message", "Permissions mises à jour avec succès."));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 3. Suspendre (bloquer) ou réactiver un sous-admin
     */
    @PutMapping("/{id}/block")
    public ResponseEntity<?> toggleBlockAdmin(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {

        return userRepository.findById(id).map(admin -> {
            if (admin.isSuperAdmin()) {
                return ResponseEntity.badRequest().body("Le Super Admin ne peut pas être bloqué.");
            }

            boolean isBlocked = (boolean) request.getOrDefault("isBlocked", false);
            admin.setBlocked(isBlocked);

            if (isBlocked) {
                if (request.get("blockedUntil") != null) {
                    String dateStr = (String) request.get("blockedUntil");
                    admin.setBlockedUntil(LocalDate.parse(dateStr));
                } else {
                    admin.setBlockedUntil(null);
                }
            } else {
                admin.setBlockedUntil(null);
            }

            userRepository.save(admin);
            String message = isBlocked ? "Administrateur suspendu avec succès." : "Administrateur réactivé.";
            return ResponseEntity.ok().body(Map.of("message", message));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 4. NOUVEAU : Supprimer définitivement un sous-administrateur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubAdmin(@PathVariable String id) {
        return userRepository.findById(id).map(admin -> {
            if (admin.isSuperAdmin()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Impossible de supprimer le Super Admin."));
            }
            userRepository.deleteById(id);
            return ResponseEntity.ok().body(Map.of("message", "Administrateur supprimé avec succès."));
        }).orElse(ResponseEntity.notFound().build());
    }

    private String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}