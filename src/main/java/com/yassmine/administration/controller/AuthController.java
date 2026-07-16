package com.yassmine.administration.controller;

import com.yassmine.administration.dto.request.LoginRequest;
import com.yassmine.administration.dto.request.RegisterRequest;
import com.yassmine.administration.dto.response.ApiResponse;
import com.yassmine.administration.dto.response.JwtResponse;
import com.yassmine.administration.exception.AccountBlockedException;
import com.yassmine.administration.model.User;
import com.yassmine.administration.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Connexion réussie"));
        } catch (AccountBlockedException e) {
            // 🛡️ Compte bloqué : 403 Forbidden avec un message clair pour le frontend.
            // On utilise une Map neutre (plutôt que de deviner le constructeur exact de ApiResponse)
            // pour garder la même forme JSON { success, message, data } que le reste de l'API.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "data", Map.of()
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Utilisateur créé avec succès"));
    }
}