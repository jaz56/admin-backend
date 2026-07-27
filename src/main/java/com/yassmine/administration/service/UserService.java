package com.yassmine.administration.service;


import com.yassmine.administration.dto.request.CreateUserRequest;
import com.yassmine.administration.dto.request.UpdatePasswordRequest;
import com.yassmine.administration.dto.request.UpdateProfileRequest;
import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.Demande;
import com.yassmine.administration.model.Order;
import com.yassmine.administration.model.User;
import com.yassmine.administration.model.embedded.NationaliteItem;
import com.yassmine.administration.model.embedded.Pays;
import com.yassmine.administration.model.embedded.Subscription;
import com.yassmine.administration.model.embedded.TransactionDetail;
import com.yassmine.administration.repository.BookingRepository;
import com.yassmine.administration.repository.DemandeRepository;
import com.yassmine.administration.repository.OrderRepository;
import com.yassmine.administration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final DemandeRepository demandeRepository;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;
    private final MongoTemplate mongoTemplate;

    @Value("${app.base-url}")
    private String baseUrl;
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'email : " + email));
    }

    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'id : " + id));
    }

    public User update(User updatedUser) {
        User existingUser = getById(updatedUser.getId());
        System.out.println("🔍 stbInfo reçu: " + updatedUser.getStbInfo());

        // Informations personnelles
        existingUser.setNom(updatedUser.getNom());
        existingUser.setPrenom(updatedUser.getPrenom());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setSecondEmail(updatedUser.getSecondEmail());
        existingUser.setLieuDeNaissance(updatedUser.getLieuDeNaissance());
        existingUser.setNumeroTel(updatedUser.getNumeroTel());
        existingUser.setSexe(updatedUser.getSexe());
        existingUser.setDateDeNaissance(updatedUser.getDateDeNaissance());
        existingUser.setPays(updatedUser.getPays());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setCodePostal(updatedUser.getCodePostal());
        existingUser.setPieceIdentite(updatedUser.getPieceIdentite());
        existingUser.setNationalite(updatedUser.getNationalite());

        // Profil professionnel
        existingUser.setDernierePosteOccupe(updatedUser.getDernierePosteOccupe());
        existingUser.setFonction(updatedUser.getFonction());
        existingUser.setLangueDeProcedure(updatedUser.getLangueDeProcedure());
        existingUser.setInteressesPar(updatedUser.getInteressesPar());
        existingUser.setFavoriteJobs(updatedUser.getFavoriteJobs());

        // Statut & rôle
        existingUser.setRole(updatedUser.getRole());
        existingUser.setVerifiedEmail(updatedUser.isVerifiedEmail());
        existingUser.setCandidateVerificationStatus(updatedUser.getCandidateVerificationStatus());
        existingUser.setProgress(updatedUser.getProgress());

        // Financier
        existingUser.setBalance(updatedUser.getBalance());
        existingUser.setCashback(updatedUser.getCashback());
        existingUser.setBalanceTransactions(updatedUser.getBalanceTransactions());
        existingUser.setCashbackTransactions(updatedUser.getCashbackTransactions());

        // ═══ LE CHAÎNON MANQUANT EST ICI ═══
        existingUser.setStbInfo(updatedUser.getStbInfo());

        User saved = userRepository.save(existingUser);
        System.out.println("✅ stbInfo sauvegardé: " + saved.getStbInfo());
        return saved;
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
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // ── 1. Gestion du Pays ──
        Pays pays = null;
        if (request.getPays() != null) {
            pays = new Pays();
            pays.setValue(request.getPays().getValue());
            pays.setLabel(request.getPays().getLabel());
        }

        // ── 2. Gestion de la Nationalité avec ta classe NationaliteItem ──
        NationaliteItem nationaliteObj = null;
        if (request.getNationalite() != null) {
            // Utilisation propre et directe grâce à ta classe
            nationaliteObj = new NationaliteItem();
            nationaliteObj.setValue(request.getNationalite().getValue());
            nationaliteObj.setLabel(request.getNationalite().getLabel());
        }

        // ── 3. Création du User ──
        User user = User.builder()
                .email(request.getEmail())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .sexe(request.getSexe())
                .numeroTel(request.getNumeroTel())
                .pays(pays)
                .address(request.getAddress())
                .codePostal(request.getCodePostal())

                // ── MODIFICATION ICI ──
                .nationalite(nationaliteObj)
                // ──────────────────────

                .dernierePosteOccupe(request.getDernierePosteOccupe())
                .fonction(request.getFonction())
                .langueDeProcedure(request.getLangueDeProcedure())
                .pieceIdentite(request.getPieceIdentite())
                .uniqueId(generateUniqueId())
                .acceptedTerms(true)
                .nvRegister(true)
                .verifiedEmail(false)
                .candidateVerificationStatus("pending")
                .candidateVerified(false)
                .profileCompletionPercentage(0)
                .balance(0.0)
                .cashback(0.0)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }    private String generateUniqueId() {
        // Format : YY-MM-JT-U-XXXXXXXXXXXX (comme tes données existantes)
        java.time.LocalDate now = java.time.LocalDate.now();
        String year = String.valueOf(now.getYear()).substring(2);   // "26"
        String month = String.format("%02d", now.getMonthValue());  // "06"
        String random = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase()
                .substring(0, 12); // "C68DBB9113174"

        return year + "-" + month + "-JT-U-" + random;
        // Résultat ex : "26-06-JT-U-C68DBB911317"
    }
    public void deleteUserCascade(String userId) {
        // 1. Récupérer toutes les demandes de cet utilisateur
        List<Demande> demandes = demandeRepository.findByUserId(userId);

        for (Demande demande : demandes) {
            // 2. Pour chaque demande, supprimer les bookings liés
            List<Booking> bookings = bookingRepository.findByDemandeId(demande.getId());

            for (Booking booking : bookings) {
                // 3. Pour chaque booking, supprimer les orders liés
                List<Order> orders = orderRepository.findByBookingId(booking.getUniqueId());
                // adapter "findByBookingId" selon le champ exact (Order.booking_id est un uniqueId String)
                if (!orders.isEmpty()) {
                    orderRepository.deleteAll(orders);
                }
            }

            if (!bookings.isEmpty()) {
                bookingRepository.deleteAll(bookings);
            }
        }

        if (!demandes.isEmpty()) {
            demandeRepository.deleteAll(demandes);
        }

        // 4. Supprimer aussi les bookings directement liés à l'utilisateur (au cas où sans demande)
        List<Booking> directBookings = bookingRepository.findByUserId(userId);
        if (!directBookings.isEmpty()) {
            bookingRepository.deleteAll(directBookings);
        }

        // 5. Enfin, supprimer l'utilisateur
        userRepository.deleteById(userId);
    }
    // Ajout en haut de la classe si pas déjà présent :
// @Autowired private MongoTemplate mongoTemplate;

    public Page<User> getAllUsersFiltered(
            int page, int limit,
            String role, String status, String search,
            String sexe, String pays, String nationalite, String fonction,
            String numeroTel, String codePostal, String jobFavori) {  // ← ajoutés

        List<Criteria> criteriaList = new ArrayList<>();

        if (role != null && !role.isEmpty())
            criteriaList.add(Criteria.where("role").is(role));

        if (status != null && !status.isEmpty())
            criteriaList.add(Criteria.where("candidateVerificationStatus").is(status));

        if (sexe != null && !sexe.isEmpty())
            criteriaList.add(Criteria.where("sexe").is(sexe));

        if (pays != null && !pays.isEmpty())
            criteriaList.add(Criteria.where("pays.label").regex(pays.trim(), "i"));

        if (nationalite != null && !nationalite.isEmpty())
            criteriaList.add(Criteria.where("nationalite.label").regex(nationalite.trim(), "i"));
        if (fonction != null && !fonction.isEmpty())
            criteriaList.add(Criteria.where("fonction").regex(fonction.trim(), "i"));

        // ← 3 filtres ajoutés
        if (numeroTel != null && !numeroTel.isEmpty())
            criteriaList.add(Criteria.where("numeroTel").regex(numeroTel.trim(), "i"));

        if (codePostal != null && !codePostal.isEmpty())
            criteriaList.add(Criteria.where("codePostal").regex(codePostal.trim(), "i"));

        if (jobFavori != null && !jobFavori.isEmpty()) {
            // interessesPar est un tableau de strings ["travails", ...]
            // MongoDB regex fonctionne directement sur les éléments du tableau
            criteriaList.add(Criteria.where("interessesPar").regex(jobFavori.trim(), "i"));
        }

        if (search != null && !search.trim().isEmpty()) {
            String pattern = search.trim();
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("nom").regex(pattern, "i"),
                    Criteria.where("prenom").regex(pattern, "i"),
                    Criteria.where("email").regex(pattern, "i")
            );
            criteriaList.add(searchCriteria);
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        long total = mongoTemplate.count(query, User.class);
        query.with(pageable);
        List<User> users = mongoTemplate.find(query, User.class);

        return new PageImpl<>(users, pageable, total);
    }
    public String uploadUserFile(String userId, MultipartFile file, String type) {
        String relativePath = fileStorageService.store(file, userId, type);
        String fullUrl = baseUrl + relativePath;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + userId));

        switch (type) {
            case "photo"     -> user.setPhotoDeProfile(fullUrl);
            case "cinRecto"  -> user.setCinRecto(fullUrl);
            case "cinVerso"  -> user.setCinVerso(fullUrl);
            case "vocal"     -> user.setVocal(fullUrl);
            case "stb_audio" -> {
                // Ne pas écraser vocal — juste retourner l'URL
                // L'URL sera mise dans stbInfo.interviewRecording par le frontend
            }
            default -> throw new RuntimeException("Type de fichier inconnu : " + type);
        }

        userRepository.save(user);
        System.out.println("✅ Upload réussi : " + fullUrl);
        return fullUrl;
    }
    // ── ABONNEMENTS ────────────────────────────────────────────────────────

    public User addSubscription(String userId, Subscription subscription) {
        User user = getById(userId);
        if (user.getSubscriptions() == null) user.setSubscriptions(new ArrayList<>());
        subscription.setId(java.util.UUID.randomUUID().toString());
        user.getSubscriptions().add(subscription);
        return userRepository.save(user);
    }

    public User updateSubscription(String userId, String subId, Subscription updated) {
        User user = getById(userId);
        List<Subscription> subs = user.getSubscriptions();
        if (subs == null) throw new RuntimeException("Aucun abonnement trouvé");

        for (int i = 0; i < subs.size(); i++) {
            if (subId.equals(subs.get(i).getId())) {
                updated.setId(subId);
                subs.set(i, updated);
                break;
            }
        }
        user.setSubscriptions(subs);
        return userRepository.save(user);
    }

    public User deleteSubscription(String userId, String subId) {
        User user = getById(userId);
        if (user.getSubscriptions() != null) {
            user.getSubscriptions().removeIf(s -> subId.equals(s.getId()));
        }
        return userRepository.save(user);
    }

// ── TRANSACTIONS ───────────────────────────────────────────────────────

    public User addTransaction(String userId, TransactionDetail transaction) {
        User user = getById(userId);
        if (user.getBalanceTransactions() == null)
            user.setBalanceTransactions(new ArrayList<>());
        transaction.setId(java.util.UUID.randomUUID().toString());
        transaction.setCreatedAt(LocalDateTime.now());
        user.getBalanceTransactions().add(0, transaction); // ajouter en tête
        return userRepository.save(user);
    }

    public User deleteTransaction(String userId, String txId) {
        User user = getById(userId);
        if (user.getBalanceTransactions() != null) {
            user.getBalanceTransactions().removeIf(t -> txId.equals(t.getId()));
        }
        return userRepository.save(user);
    }
    public User updateStbInfo(String userId, Map<String, Object> body) {
        User user = getById(userId);

        Object stbData = body.get("stbInfo");
        if (stbData == null) return user;

        // Convertir la Map en StbInfo via ObjectMapper
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );

        try {
            com.yassmine.administration.model.embedded.StbInfo stbInfo =
                    mapper.convertValue(stbData,
                            com.yassmine.administration.model.embedded.StbInfo.class);
            user.setStbInfo(stbInfo);
            System.out.println("✅ STB updated for user: " + userId);
        } catch (Exception e) {
            throw new RuntimeException("Erreur conversion STB: " + e.getMessage());
        }

        return userRepository.save(user);
    }

}
