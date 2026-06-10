package com.yassmine.administration.controller;
import com.yassmine.administration.dto.response.DemandeResponseDTO;
import com.yassmine.administration.dto.response.ApiResponse;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.Demande;
import com.yassmine.administration.model.User;
import com.yassmine.administration.service.BookingService;
import com.yassmine.administration.service.DemandeService;
import com.yassmine.administration.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService; // Plus besoin de BookingService ici
    @Autowired
    private UserService userService;
    @PostMapping
    public ResponseEntity<ApiResponse<Demande>> createDemande(@RequestBody Demande demande, @AuthenticationPrincipal UserDetails userDetails) {
        Demande saved = demandeService.saveForUser(demande, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved, "Demande soumise"));
    }

    @GetMapping("/my-demandes")
    public ResponseEntity<ApiResponse<List<Demande>>> getMyDemandes(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(demandeService.findByUserEmail(userDetails.getUsername()), "Vos demandes"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllDemandes(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        // On trie automatiquement par date de création décroissante
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        // 💡 MODIFICATION ICI : On utilise Page<DemandeResponseDTO> au lieu de Page<Demande>
        Page<DemandeResponseDTO> demandesPage = demandeService.getAllDemandes(status, pageable);

        // On prépare la structure exacte attendue par ton demandes.component.ts
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", demandesPage.getContent());       // La liste des demandes enrichies (res.data)
        response.put("total", demandesPage.getTotalElements()); // Le nombre total (res.total)

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    // 1. On enlève le "hasRole('ADMIN')" pour que le candidat connecté puisse modifier son propre dossier !
    public ResponseEntity<ApiResponse<Demande>> update(
            @PathVariable String id,
            @RequestBody Demande demande,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 2. On récupère l'utilisateur connecté
        User user = userService.getByEmail(userDetails.getUsername());

        // 3. On passe bien les 3 arguments (id, données, et ID utilisateur pour la sécurité)
        Demande updatedDemande = demandeService.updateDemande(id, demande, user.getId());

        return ResponseEntity.ok(ApiResponse.success(updatedDemande, "Demande mise à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {
        demandeService.deleteDemande(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Demande supprimée avec succès"));
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Demande>> updateDemandeStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) { // 👈 On utilise @RequestBody pour lire ce qu'Angular envoie

        // On récupère le statut envoyé par Angular
        String status = payload.get("status");

        // Si ta méthode de service a absolument besoin de "progress",
        // on peut utiliser la même valeur que "status", ou ce que tu avais prévu.
        String progress = payload.getOrDefault("progress", status);

        Demande updated = demandeService.updateProgress(id, progress, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Le statut de la demande a été mis à jour par l'admin"));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Demande>> getMyDemande(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByEmail(userDetails.getUsername());
        Demande demande = demandeService.getMyDemande(user.getId());
        return ResponseEntity.ok(ApiResponse.success(demande, "Dossier récupéré"));
    }

}