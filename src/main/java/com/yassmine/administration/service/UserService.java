package com.yassmine.administration.service;


import com.yassmine.administration.dto.request.UpdatePasswordRequest;
import com.yassmine.administration.dto.request.UpdateProfileRequest;
import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'email : " + email));
    }

    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'id : " + id));
    }

    public User update(User updatedUser) {
        // On récupère l'utilisateur existant pour ne pas écraser les champs critiques (password, etc.)
        User existingUser = getById(updatedUser.getId());

        existingUser.setNom(updatedUser.getNom());
        existingUser.setPrenom(updatedUser.getPrenom());
        existingUser.setNumeroTel(updatedUser.getNumeroTel());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setCodePostal(updatedUser.getCodePostal());
        existingUser.setSexe(updatedUser.getSexe());
        existingUser.setPays(updatedUser.getPays());
        existingUser.setProgress(updatedUser.getProgress());
        existingUser.setDernierePosteOccupe(updatedUser.getDernierePosteOccupe());
        existingUser.setFonction(updatedUser.getFonction());
        existingUser.setBalance(updatedUser.getBalance());
        existingUser.setCashback(updatedUser.getCashback());
        existingUser.setBalanceTransactions(updatedUser.getBalanceTransactions());
        existingUser.setCashbackTransactions(updatedUser.getCashbackTransactions());

        return userRepository.save(existingUser);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }
    // À AJOUTER dans UserService.java

    // DELETE : Supprimer un utilisateur
    public void deleteUser(String id) {
        User user = getById(id);
        userRepository.deleteById(user.getId());
    }
    public Page<User> getAllUsersPaginated(int page, int limit, String role) {
        Pageable pageable = PageRequest.of(page, limit);

        // S'il y a un filtre de rôle (candidat, admin...), on filtre
        if (role != null && !role.trim().isEmpty()) {
            return userRepository.findByRole(role, pageable);
        }

        // Sinon on renvoie tous les utilisateurs
        return userRepository.findAll(pageable);
    }
    public User updateProfile(String currentEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail()); // Attention : si l'email change, le token actuel restera valide jusqu'à expiration, mais le prochain login utilisera le nouvel email.

        return userRepository.save(user);
    }

    public void updatePassword(String currentEmail, UpdatePasswordRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier si le mot de passe actuel correspond
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Le mot de passe actuel est incorrect");
        }

        // Mettre à jour avec le nouveau mot de passe encodé
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
