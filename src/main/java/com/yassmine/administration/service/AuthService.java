package com.yassmine.administration.service;

import com.yassmine.administration.dto.request.LoginRequest;
import com.yassmine.administration.dto.request.RegisterRequest;
import com.yassmine.administration.dto.response.JwtResponse;
import com.yassmine.administration.exception.AccountBlockedException;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.UserRepository;
import com.yassmine.administration.security.JwtUtil;
import com.yassmine.administration.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 🛡️ VÉRIFICATION DU BLOCAGE — se fait AVANT toute émission de token.
        // Un compte bloqué ne peut donc jamais obtenir de JWT valide, même via un appel API direct.
        if (!user.isSuperAdmin() && user.isBlocked()) {
            if (user.getBlockedUntil() != null) {
                if (LocalDate.now().isAfter(user.getBlockedUntil())) {
                    // Période de suspension écoulée : réactivation automatique (même logique que AdminPermissionFilter)
                    user.setBlocked(false);
                    user.setBlockedUntil(null);
                    userRepository.save(user);
                } else {
                    throw new AccountBlockedException(
                            "Votre compte est suspendu jusqu'au " + user.getBlockedUntil().format(DATE_FORMAT) + "."
                    );
                }
            } else {
                throw new AccountBlockedException(
                        "Votre compte a été suspendu indéfiniment par le Super Administrateur."
                );
            }
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return JwtResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .photoDeProfile(user.getPhotoDeProfile())
                .balance(user.getBalance())
                .candidateVerificationStatus(user.getCandidateVerificationStatus())
                .isSuperAdmin(user.isSuperAdmin())
                .permissions(user.getPermissions())
                .permissionsExpiryDate(user.getPermissionsExpiryDate())
                .isBlocked(user.isBlocked())
                .blockedUntil(user.getBlockedUntil())
                .build();
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        User user = User.builder()
                .email(request.getEmail())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .role(request.getRole().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .acceptedTerms(request.getAcceptedTerms())
                .balance(0.0)
                .cashback(0.0)
                .candidateVerificationStatus("pending")
                .build();

        return userRepository.save(user);
    }
}