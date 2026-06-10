package com.yassmine.administration.controller;

import com.yassmine.administration.dto.request.UpdatePasswordRequest;
import com.yassmine.administration.dto.request.UpdateProfileRequest;
import com.yassmine.administration.dto.response.ApiResponse;
import com.yassmine.administration.model.User;
import com.yassmine.administration.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user, "Profil récupéré"));
    }



    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String role) {

        // Appel au service pour récupérer la page
        org.springframework.data.domain.Page<User> userPage = userService.getAllUsersPaginated(page, limit, role);

        // On construit la réponse EXACTEMENT comme Angular l'attend (res.data et res.total)
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", userPage.getContent());
        response.put("total", userPage.getTotalElements());

        return ResponseEntity.ok(response);
    }
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        User updatedUser = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Profil mis à jour avec succès"));
    }

    // Remplacer l'ancienne méthode par celle-ci :
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdatePasswordRequest request) {

        // On n'envoie plus passwordEncoder en paramètre
        userService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Mot de passe mis à jour avec succès"));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Optionnel : protège la route si seuls les admins peuvent voir les détails
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        User user = userService.getById(id);
        // On encapsule l'utilisateur dans l'ApiResponse pour que Angular retrouve res.data
        return ResponseEntity.ok(ApiResponse.success(user, "Utilisateur récupéré avec succès"));
    }

}
