package com.yassmine.administration.service;

import com.yassmine.administration.dto.request.LoginRequest;
import com.yassmine.administration.dto.request.RegisterRequest;
import com.yassmine.administration.dto.response.JwtResponse;
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

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtResponse login(LoginRequest request) {
        // Vérifie email + password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // Dans AuthService.java, modifiez le retour de la méthode login :
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
                .password(passwordEncoder.encode(request.getPassword())) // Hachage automatique en BCrypt
                .acceptedTerms(request.getAcceptedTerms())
                .balance(BigDecimal.ZERO)
                .cashback(BigDecimal.ZERO)
                .candidateVerificationStatus("pending")
                .build();

        return userRepository.save(user);
    }
}
