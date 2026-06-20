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

import java.util.HashMap;
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




    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllDemandes(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String journeesDestination,
            @RequestParam(required = false) String niveauEtude,
            @RequestParam(required = false) String condidatStatutActuel,
            @RequestParam(required = false) String fonction,
            @RequestParam(required = false) String posteSouhaite,
            @RequestParam(required = false) String nombreAnneesExperience,
            @RequestParam(required = false) String paysResidence,
            @RequestParam(required = false) Boolean besoinVisa,
            @RequestParam(required = false) Boolean existenceDeGarant,
            @RequestParam(required = false) Boolean typeHebergement,
            @RequestParam(required = false) Boolean preinscription,
            @RequestParam(required = false) Boolean handicape,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        Page<DemandeResponseDTO> result = demandeService.getAllDemandesFiltered(
                status, type, journeesDestination, niveauEtude,
                condidatStatutActuel, fonction, posteSouhaite,
                nombreAnneesExperience, paysResidence,
                besoinVisa, existenceDeGarant, typeHebergement,
                preinscription, handicape, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getContent());
        response.put("total", result.getTotalElements());
        return ResponseEntity.ok(response);
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Demande>> getById(@PathVariable String id) {
        Demande demande = demandeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(demande, "Demande récupérée"));
    }
    // DemandeController.java
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Demande>> createDemandeAsAdmin(
            @RequestBody Map<String, Object> payload) {
        Demande saved = demandeService.createForUserId(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saved, "Demande créée avec succès"));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        demandeService.deleteDemandeAsAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Demande et bookings associés supprimés avec succès"));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Demande>> update(
            @PathVariable String id,
            @RequestBody Demande demande) {
        return ResponseEntity.ok(
                ApiResponse.success(demandeService.update(id, demande), "Demande mise à jour")
        );
    }

}