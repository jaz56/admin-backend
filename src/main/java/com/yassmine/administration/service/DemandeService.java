package com.yassmine.administration.service;


import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Demande;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final UserService userService;

    public Demande saveForUser(Demande demande, String userEmail) {
        User user = userService.getByEmail(userEmail);

        demande.setUserId(user.getId());

        if (demande.getUniqueId() == null || demande.getUniqueId().isEmpty()) {
            String shortId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            demande.setUniqueId("26-06-T-" + shortId);
        }

        return demandeRepository.save(demande);
    }

    public List<Demande> findByUserEmail(String userEmail) {
        User user = userService.getByEmail(userEmail);
        return demandeRepository.findByUserId(user.getId()); // Utilise l'ID maintenant
    }

    public Demande getById(String id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable avec l'id : " + id));
    }

    // UPDATE : Mettre à jour le statut ou la progression (ex: Passer à l'étape suivante)
    public Demande updateProgress(String id, String newProgress, String newStatus) {
        Demande demande = getById(id);
        demande.setProgress(newProgress);
        demande.setStatus(newStatus);
        return demandeRepository.save(demande);
    }

    // UPDATE : Mise à jour globale des données du formulaire
    public Demande updateDemande(String id, Demande updatedData, String userId) {
        // 1. Récupération du dossier existant
        Demande existing = getById(id);

        // 2. SÉCURITÉ : On vérifie si le dossier appartient bien à l'utilisateur connecté
        // 🔥 Version plus stricte et sécurisée
        if (existing.getUserId() == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("Action non autorisée : Ce dossier ne vous appartient pas.");
        }

        // Étape 1 du formulaire Angular (Infos générales)
        existing.setType(updatedData.getType());
        existing.setJourneesDestination(updatedData.getJourneesDestination());
        existing.setPaysResidence(updatedData.getPaysResidence());
        existing.setBesoinVisa(updatedData.isBesoinVisa());
        existing.setTypeHebergement(updatedData.isTypeHebergement());
        existing.setMaladieContagieuse(updatedData.isMaladieContagieuse());
        existing.setHandicape(updatedData.isHandicape());
        existing.setExistenceDeGarant(updatedData.isExistenceDeGarant());
        existing.setOuverteTouteOpportunites(updatedData.isOuverteTouteOpportunites());

        // Étape 2 du formulaire Angular (Parcours professionnel)
        existing.setNiveauEtude(updatedData.getNiveauEtude());
        existing.setNombreAnneesEtude(updatedData.getNombreAnneesEtude());
        existing.setFonction(updatedData.getFonction());
        existing.setActivite(updatedData.getActivite());
        existing.setPosteSouhaite(updatedData.getPosteSouhaite());
        existing.setNombreMoisStage(updatedData.getNombreMoisStage());
        existing.setCondidatStatutActuel(updatedData.getCondidatStatutActuel());
        existing.setDernierPosteOccupe(updatedData.getDernierPosteOccupe());
        existing.setNombreAnneesExperience(updatedData.getNombreAnneesExperience());

        // Étape 3 du formulaire Angular (Langues & Diplômes)
        existing.setDiplomes(updatedData.getDiplomes());
        existing.setConnaissanceLinguistique(updatedData.getConnaissanceLinguistique());

        // 3. Sauvegarde en base MongoDB
        return demandeRepository.save(existing);
    }
    // DELETE : Supprimer une demande
    public void deleteDemande(String id, String userEmail) {
        Demande demande = getById(id);
        User user = userService.getByEmail(userEmail); // On récupère l'utilisateur connecté

        if (!demande.getUserId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Vous n'avez pas le droit de supprimer cette demande");
        }
        demandeRepository.deleteById(id);
    }
    // NOUVEAU : Récupérer toutes les demandes (Réservé à l'Admin)
    // NOUVEAU : Récupérer toutes les demandes enrichies avec le Nom/Prénom (Réservé à l'Admin)
    public Page<com.yassmine.administration.dto.response.DemandeResponseDTO> getAllDemandes(String status, Pageable pageable) {
        Page<Demande> demandesPage;

        if (status != null && !status.isEmpty()) {
            demandesPage = demandeRepository.findByStatus(status, pageable);
        } else {
            demandesPage = demandeRepository.findAll(pageable);
        }

        // On transforme (map) chaque Demande en DemandeResponseDTO en allant chercher l'utilisateur
        return demandesPage.map(demande -> {
            com.yassmine.administration.dto.response.DemandeResponseDTO dto =
                    com.yassmine.administration.dto.response.DemandeResponseDTO.builder()
                            .id(demande.getId())
                            .uniqueId(demande.getUniqueId())
                            .type(demande.getType())
                            .status(demande.getStatus())
                            .journeesDestination(demande.getJourneesDestination())
                            .niveauEtude(demande.getNiveauEtude())
                            .createdAt(demande.getCreatedAt())
                            .build();

            // Si la demande contient un userId, on récupère l'User via le userService
            if (demande.getUserId() != null) {
                try {
                    User user = userService.getById(demande.getUserId()); // Assure-toi que getById(String id) existe dans ton userService
                    if (user != null) {
                        dto.setCandidatNom(user.getNom());
                        dto.setCandidatPrenom(user.getPrenom());
                    }
                } catch (Exception e) {
                    // Si l'utilisateur n'est pas trouvé (ex: supprimé), on laisse nom/prénom à null
                    dto.setCandidatNom(null);
                    dto.setCandidatPrenom(null);
                }
            }
            return dto;
        });
    }
    public Demande getMyDemande(String userId) {
        List<Demande> demandes = demandeRepository.findByUserId(userId);
        if (demandes != null && !demandes.isEmpty()) {
            // On renvoie la première demande trouvée (ou la plus récente si tu les tries)
            return demandes.get(0);
        }
        return null;
    }
}