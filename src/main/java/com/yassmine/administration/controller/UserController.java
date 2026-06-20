package com.yassmine.administration.controller;

import com.yassmine.administration.dto.request.CreateUserRequest;
import com.yassmine.administration.dto.request.UpdatePasswordRequest;
import com.yassmine.administration.dto.request.UpdateProfileRequest;
import com.yassmine.administration.dto.response.ApiResponse;
import com.yassmine.administration.model.User;
import com.yassmine.administration.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sexe,
            @RequestParam(required = false) String pays,
            @RequestParam(required = false) String nationalite,
            @RequestParam(required = false) String fonction,
            @RequestParam(required = false) String numeroTel,    // ← ajouté
            @RequestParam(required = false) String codePostal,   // ← ajouté
            @RequestParam(required = false) String jobFavori) {  // ← ajouté

        Page<User> usersPage = userService.getAllUsersFiltered(
                page, limit, role, status, search,
                sexe, pays, nationalite, fonction,
                numeroTel, codePostal, jobFavori);   // ← ajoutés

        Map<String, Object> response = new HashMap<>();
        response.put("data", usersPage.getContent());
        response.put("total", usersPage.getTotalElements());
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
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody CreateUserRequest request) {
        User createdUser = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(createdUser, "Utilisateur créé avec succès"));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable String id,
            @RequestBody User updatedUser) {
        updatedUser.setId(id);
        User saved = userService.update(updatedUser);
        return ResponseEntity.ok(ApiResponse.success(saved, "Utilisateur mis à jour"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        userService.deleteUserCascade(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Utilisateur et toutes ses données associées supprimés avec succès"));
    }
    // ─── AJOUT DE L'ENDPOINT D'UPLOAD MANQUANT ───
    @PostMapping("/{id}/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @PathVariable String id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("type") String type) {

        // On appelle une méthode dans le service qui gère l'écriture disque et retourne l'URL
        String fileUrl = userService.uploadUserFile(id, file, type);

        Map<String, String> response = new HashMap<>();
        response.put("url", fileUrl);

        return ResponseEntity.ok(ApiResponse.success(response, "Fichier téléversé avec succès"));
    }
}
