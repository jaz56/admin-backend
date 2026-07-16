package com.yassmine.administration.service;


import com.yassmine.administration.dto.request.AdminCreateRequestDTO;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // Votre service d'envoi d'emails

    public User createSubAdmin(AdminCreateRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        // 1. Génération d'un mot de passe sécurisé à 12 caractères
        String rawPassword = generateSecurePassword();

        // 2. Construction de l'administrateur
        User subAdmin = User.builder()
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(rawPassword)) // Toujours encodé en BDD
                .role("admin")
                .isSuperAdmin(false)
                .permissions(dto.getPermissions())
                .permissionsExpiryDate(dto.getPermissionsExpiryDate())
                .nvRegister(true)
                .verifiedEmail(true) // On considère l'email vérifié puisque créé par le Super Admin
                .build();

        User savedAdmin = userRepository.save(subAdmin);

        // 3. Envoi de l'email de bienvenue avec les identifiants en clair
        emailService.sendAdminWelcomeEmail(savedAdmin, rawPassword);

        return savedAdmin;
    }

    private String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
