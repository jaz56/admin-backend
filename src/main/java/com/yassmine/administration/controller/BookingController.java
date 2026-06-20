package com.yassmine.administration.controller;

import com.yassmine.administration.dto.response.ApiResponse;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.User;
import com.yassmine.administration.service.BookingService;
import com.yassmine.administration.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*") // Optionnel : à adapter selon tes besoins CORS
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    private UserService userService;

    /**
     * AJUSTÉ : Route racine /api/bookings pour correspondre au POST Angular
     */

    /**
     * ALIGNÉ : Permet au candidat de récupérer sa réservation active
     */

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<Booking>> reschedule(
            @PathVariable String id,
            @RequestParam java.time.LocalDateTime date,
            @RequestParam String time) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.reschedule(id, date, time), "Rendez-vous déplacé"));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Booking>> cancel(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.cancelBooking(id), "Rendez-vous annulé"));
    }

    @PatchMapping("/{id}/score")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")    public ResponseEntity<ApiResponse<Booking>> updateScore(
            @PathVariable String id,
            @RequestParam BigDecimal score) {
        Booking updated = bookingService.updateInterviewScore(id, score);
        return ResponseEntity.ok(ApiResponse.success(updated, "Score mis à jour"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")    public ResponseEntity<Map<String, Object>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        org.springframework.data.domain.Page<Booking> bookingPage = bookingService.getAllBookingsPaginated(status, page, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("data", bookingPage.getContent());
        response.put("total", bookingPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/finalise")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")    public ResponseEntity<ApiResponse<Booking>> finaliseInterview(
            @PathVariable String id,
            @RequestParam String report,
            @RequestParam String recordingUrl) {

        Booking updated = bookingService.finaliseInterview(id, report, recordingUrl);
        return ResponseEntity.ok(ApiResponse.success(updated, "Compte-rendu d'entretien enregistré avec succès"));
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Booking>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {

        String status = payload.get("status");

        // Remplace "updateBookingStatus" par le nom exact de la méthode de ton BookingService qui gère la mise à jour
        Booking updated = bookingService.updateBookingStatus(id, status);

        return ResponseEntity.ok(ApiResponse.success(updated, "Statut de la réservation mis à jour"));
    }
    @PostMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Booking>> createAsAdmin(@RequestBody Map<String, Object> payload) {
        Booking saved = bookingService.createForDemande(payload);
        return ResponseEntity.ok(ApiResponse.success(saved, "Booking créé avec succès"));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        bookingService.deletePermanent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking supprimé avec succès"));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Booking>> getById(@PathVariable String id) {
        Booking booking = bookingService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking récupéré"));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Booking>> update(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        Booking updated = bookingService.updateBookingFields(id, payload);
        return ResponseEntity.ok(ApiResponse.success(updated, "Booking mis à jour avec succès"));
    }
}