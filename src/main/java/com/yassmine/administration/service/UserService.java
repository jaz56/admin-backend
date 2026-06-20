package com.yassmine.administration.service;


import com.yassmine.administration.dto.request.CreateUserRequest;
import com.yassmine.administration.dto.request.UpdatePasswordRequest;
import com.yassmine.administration.dto.request.UpdateProfileRequest;
import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.Demande;
import com.yassmine.administration.model.Order;
import com.yassmine.administration.model.User;
import com.yassmine.administration.model.embedded.Pays;
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
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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

        // Informations personnelles
        existingUser.setNom(updatedUser.getNom());
        existingUser.setPrenom(updatedUser.getPrenom());
        existingUser.setEmail(updatedUser.getEmail());                   // ← AJOUT
        existingUser.setNumeroTel(updatedUser.getNumeroTel());
        existingUser.setSexe(updatedUser.getSexe());
        existingUser.setDateDeNaissance(updatedUser.getDateDeNaissance()); // ← AJOUT
        existingUser.setPays(updatedUser.getPays());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setCodePostal(updatedUser.getCodePostal());
        existingUser.setPieceIdentite(updatedUser.getPieceIdentite());   // ← AJOUT
        existingUser.setNationalite(updatedUser.getNationalite());       // ← AJOUT

        // Profil professionnel
        existingUser.setDernierePosteOccupe(updatedUser.getDernierePosteOccupe());
        existingUser.setFonction(updatedUser.getFonction());
        existingUser.setLangueDeProcedure(updatedUser.getLangueDeProcedure()); // ← AJOUT
        existingUser.setInteressesPar(updatedUser.getInteressesPar());   // ← AJOUT
        existingUser.setFavoriteJobs(updatedUser.getFavoriteJobs());     // ← AJOUT

        // Statut & rôle
        existingUser.setRole(updatedUser.getRole());                     // ← AJOUT
        existingUser.setVerifiedEmail(updatedUser.isVerifiedEmail());    // ← AJOUT
        existingUser.setCandidateVerificationStatus(
                updatedUser.getCandidateVerificationStatus());               // ← AJOUT
        existingUser.setProgress(updatedUser.getProgress());             // ← AJOUT

        // Financier
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
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        Pays pays = null;
        if (request.getPays() != null) {
            pays = new Pays();
            pays.setValue(request.getPays().getValue());
            pays.setLabel(request.getPays().getLabel());
        }

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
                .nationalite(request.getNationalite() != null ? request.getNationalite() : new ArrayList<>())
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
                .balance(java.math.BigDecimal.ZERO)
                .cashback(java.math.BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }
    private String generateUniqueId() {
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
            criteriaList.add(Criteria.where("nationalite").regex(nationalite.trim(), "i"));

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
        // 1. Récupérer l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        try {
            // 2. Définir le sous-dossier selon le type (photo, cinRecto, cinVerso, vocal)
            String subFolder = "photo".equals(type) ? "photo" : ("vocal".equals(type) ? "vocal" : "documents");
            Path uploadDir = Paths.get("uploads/users/" + userId + "/" + subFolder);

            // Créer les dossiers s'ils n'existent pas
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 3. Nettoyer et définir le nom du fichier
            String extension = "vocal".equals(type) ? ".wav" : ".jpg";
            String fileName = type + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadDir.resolve(fileName);

            // 4. Copier le fichier sur le disque dur
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 5. Construire l'URL publique que le frontend va appeler
            String fileUrl = "http://localhost:8083/uploads/users/" + userId + "/" + subFolder + "/" + fileName;

            // 6. Mettre à jour le bon champ de l'utilisateur en BDD selon le type
            switch (type) {
                case "photo":
                    user.setPhotoDeProfile(fileUrl); // Remplacez par le nom exact de votre propriété
                    break;
                case "cinRecto":
                    user.setCinRecto(fileUrl);
                    break;
                case "cinVerso":
                    user.setCinVerso(fileUrl);
                    break;
                case "vocal":
                    user.setVocal(fileUrl);
                    break;
                default:
                    throw new IllegalArgumentException("Type d'upload inconnu: " + type);
            }

            // Enregistrer en base MongoDB
            userRepository.save(user);

            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'écriture du fichier sur le serveur", e);
        }
    }
}
