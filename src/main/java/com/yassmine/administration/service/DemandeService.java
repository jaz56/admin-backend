package com.yassmine.administration.service;

import com.yassmine.administration.dto.response.DemandeResponseDTO;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.repository.BookingRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Demande;
import com.yassmine.administration.model.User;
import com.yassmine.administration.model.embedded.Pays;
import com.yassmine.administration.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final BookingRepository bookingRepository;


    public Demande getById(String id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
        System.out.println("✅ Progress récupéré : " + demande.getProgress());
        return demande;
    }

    // UPDATE : Mettre à jour le statut ou la progression (ex: Passer à l'étape suivante)
    public Demande updateProgress(String id, String newProgress, String newStatus) {
        Demande demande = getById(id);
        demande.setProgress(newProgress);
        demande.setStatus(newStatus);
        return demandeRepository.save(demande);
    }

    // UPDATE : Mise à jour globale des données du formulaire
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
                            .progress(demande.getProgress())
                            .paysResidence(demande.getPaysResidence())
                            .fonction(demande.getFonction())
                            .build();

            // Si la demande contient un userId, on récupère l'User via le userService
            if (demande.getUserId() != null) {
                try {
                    User user = userService.getById(demande.getUserId()); // Assure-toi que getById(String id) existe dans ton userService
                    if (user != null) {
                        dto.setCandidatNom(user.getNom());
                        dto.setCandidatPrenom(user.getPrenom());
                        dto.setCandidatEmail(user.getEmail());        // ← ajouter
                        dto.setCandidatTel(user.getNumeroTel());      // ← ajouter
                        dto.setCandidatUniqueId(user.getUniqueId());  // ← ajouter
                    }
                } catch (Exception e) {
                    // Si l'utilisateur n'est pas trouvé (ex: supprimé), on laisse nom/prénom à null
                    dto.setCandidatNom(null);
                    dto.setCandidatPrenom(null);
                    dto.setCandidatEmail(null);        // ← ajouter
                    dto.setCandidatTel(null);          // ← ajouter
                    dto.setCandidatUniqueId(null);     // ← ajouter
                }
            }
            return dto;
        });
    }

    public Demande updateDemandeAsAdmin(String id, Demande updated) {
        Demande existing = getById(id);

        // ✅ Sauvegarder l'ANCIEN progress AVANT modification
        String oldProgress = existing.getProgress();

        if (updated.getUserId() != null && !updated.getUserId().isEmpty()) {
            existing.setUserId(updated.getUserId());
        }
        existing.setType(updated.getType());
        existing.setJourneesDestination(updated.getJourneesDestination());
        existing.setPaysResidence(updated.getPaysResidence());
        existing.setEligibiliteNote(updated.getEligibiliteNote());
        existing.setBesoinVisa(updated.isBesoinVisa());
        existing.setTypeHebergement(updated.isTypeHebergement());
        existing.setExistenceDeGarant(updated.isExistenceDeGarant());
        existing.setOuverteTouteOpportunites(updated.isOuverteTouteOpportunites());
        existing.setPreinscription(updated.isPreinscription());
        existing.setMaladieContagieuse(updated.isMaladieContagieuse());
        existing.setHandicape(updated.isHandicape());
        existing.setNiveauEtude(updated.getNiveauEtude());
        existing.setNombreAnneesEtude(updated.getNombreAnneesEtude());
        existing.setNombreMoisStage(updated.getNombreMoisStage());
        existing.setCondidatStatutActuel(updated.getCondidatStatutActuel());
        existing.setDernierPosteOccupe(updated.getDernierPosteOccupe());
        existing.setFonction(updated.getFonction());
        existing.setNombreAnneesExperience(updated.getNombreAnneesExperience());
        existing.setActivite(updated.getActivite());
        existing.setPosteSouhaite(updated.getPosteSouhaite());
        existing.setDiplomes(updated.getDiplomes());
        existing.setConnaissanceLinguistique(updated.getConnaissanceLinguistique());
        existing.setProgress(updated.getProgress());

        // ✅ Comparer avec oldProgress
        if (updated.getProgress() != null &&
                updated.getProgress().contains("STEP_2") &&
                (oldProgress == null || !oldProgress.contains("STEP_2"))) {

            autoCreateBooking(existing);
        }

        return demandeRepository.save(existing);
    }
    public Demande createForUserId(Map<String, Object> payload) {
        String userId = (String) payload.get("user");


        Demande demande = new Demande();
        demande.setUserId(userId); // adapter selon votre champ @Field("user")
        demande.setType((String) payload.get("type"));
        demande.setStatus((String) payload.getOrDefault("status", "en_attente"));
        demande.setProgress("TRAVAIL_STEP_1_SUBSTEP_5");
        demande.setJourneesDestination((String) payload.get("journeesDestination"));
        demande.setEligibiliteNote((String) payload.get("eligibiliteNote"));

        // paysResidence
        Map<String, Object> pays = (Map<String, Object>) payload.get("paysResidence");
        if (pays != null) {
            Pays p = new Pays();
            p.setValue((String) pays.get("value"));
            p.setLabel((String) pays.get("label"));
            demande.setPaysResidence(p);
        }

        demande.setBesoinVisa((Boolean) payload.getOrDefault("besoinVisa", false));
        demande.setTypeHebergement((Boolean) payload.getOrDefault("typeHebergement", false));
        demande.setExistenceDeGarant((Boolean) payload.getOrDefault("existenceDeGarant", false));
        demande.setOuverteTouteOpportunites((Boolean) payload.getOrDefault("ouverteTouteOpportunites", false));
        demande.setPreinscription((Boolean) payload.getOrDefault("preinscription", false));
        demande.setMaladieContagieuse((Boolean) payload.getOrDefault("maladieContagieuse", false));
        demande.setHandicape((Boolean) payload.getOrDefault("handicape", false));

        demande.setNiveauEtude((String) payload.get("niveauEtude"));
        demande.setNombreAnneesEtude((Integer) payload.getOrDefault("nombreAnneesEtude", 0));
        demande.setNombreMoisStage((Integer) payload.getOrDefault("nombreMoisStage", 0));
        demande.setCondidatStatutActuel((String) payload.get("condidatStatutActuel"));
        demande.setDernierPosteOccupe((String) payload.get("dernierPosteOccupe"));
        demande.setFonction((String) payload.get("fonction"));
        demande.setNombreAnneesExperience((String) payload.get("nombreAnneesExperience"));
        demande.setActivite((String) payload.get("activite"));
        demande.setPosteSouhaite((String) payload.get("posteSouhaite"));

        // diplomes & langues — laisser vide, on les ajoute après création
        demande.setDiplomes(new ArrayList<>());
        demande.setConnaissanceLinguistique(new ArrayList<>());
        demande.setAnneesDeEtudeAFinancier(new ArrayList<>());

        // uniqueId
        String dateStr = new SimpleDateFormat("yy-MM").format(new Date());
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        demande.setUniqueId(dateStr + "-T-" + randomPart);

        return demandeRepository.save(demande);
    }
    public Page<DemandeResponseDTO> getAllDemandesFiltered(
            String status, String type, String journeesDestination, String niveauEtude,
            String condidatStatutActuel, String fonction, String posteSouhaite,
            String nombreAnneesExperience, String paysResidence,
            Boolean besoinVisa, Boolean existenceDeGarant, Boolean typeHebergement,
            Boolean preinscription, Boolean handicape,
            String progress, // ◄ 1. AJOUT DU PARAMÈTRE ICI
            Pageable pageable) {

        List<Criteria> criteriaList = new ArrayList<>();

        if (status != null && !status.isEmpty())
            criteriaList.add(Criteria.where("status").is(status));
        if (type != null && !type.isEmpty())
            criteriaList.add(Criteria.where("type").is(type));
        if (journeesDestination != null && !journeesDestination.isEmpty())
            criteriaList.add(Criteria.where("journeesDestination").is(journeesDestination));
        if (niveauEtude != null && !niveauEtude.isEmpty())
            criteriaList.add(Criteria.where("niveauEtude").is(niveauEtude));
        if (condidatStatutActuel != null && !condidatStatutActuel.isEmpty())
            criteriaList.add(Criteria.where("condidatStatutActuel").is(condidatStatutActuel));
        if (nombreAnneesExperience != null && !nombreAnneesExperience.isEmpty())
            criteriaList.add(Criteria.where("nombreAnneesExperience").is(nombreAnneesExperience));

        // ◄ 2. AJOUT DE LA CONDITION DE FILTRAGE MONGO DB ICI
        if (progress != null && !progress.trim().isEmpty()) {
            criteriaList.add(Criteria.where("progress").is(progress.trim()));
        }

        if (fonction != null && !fonction.trim().isEmpty())
            criteriaList.add(Criteria.where("fonction").regex(fonction.trim(), "i"));
        if (posteSouhaite != null && !posteSouhaite.trim().isEmpty())
            criteriaList.add(Criteria.where("posteSouhaite").regex(posteSouhaite.trim(), "i"));
        if (paysResidence != null && !paysResidence.trim().isEmpty())
            criteriaList.add(Criteria.where("paysResidence.label").regex(paysResidence.trim(), "i"));

        if (besoinVisa != null)
            criteriaList.add(Criteria.where("besoinVisa").is(besoinVisa));
        if (existenceDeGarant != null)
            criteriaList.add(Criteria.where("existenceDeGarant").is(existenceDeGarant));
        if (typeHebergement != null)
            criteriaList.add(Criteria.where("typeHebergement").is(typeHebergement));
        if (preinscription != null)
            criteriaList.add(Criteria.where("preinscription").is(preinscription));
        if (handicape != null)
            criteriaList.add(Criteria.where("handicape").is(handicape));

        Query query = new Query();
        if (!criteriaList.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        long total = mongoTemplate.count(query, Demande.class);
        query.with(pageable);
        List<Demande> demandes = mongoTemplate.find(query, Demande.class);

        List<DemandeResponseDTO> dtos = demandes.stream().map(demande -> {
            DemandeResponseDTO dto = DemandeResponseDTO.builder()
                    .id(demande.getId())
                    .uniqueId(demande.getUniqueId())
                    .type(demande.getType())
                    .status(demande.getStatus())
                    .journeesDestination(demande.getJourneesDestination())
                    .niveauEtude(demande.getNiveauEtude())
                    .createdAt(demande.getCreatedAt())
                    .progress(demande.getProgress())
                    .paysResidence(demande.getPaysResidence())
                    .fonction(demande.getFonction())
                    .build();

            if (demande.getUserId() != null) {
                try {
                    User user = userService.getById(demande.getUserId());
                    if (user != null) {
                        dto.setCandidatNom(user.getNom());
                        dto.setCandidatPrenom(user.getPrenom());
                        dto.setCandidatEmail(user.getEmail());
                        dto.setCandidatTel(user.getNumeroTel());
                        dto.setCandidatUniqueId(user.getUniqueId());
                    }
                } catch (Exception ignored) {}
            }
            return dto;
        }).collect(java.util.stream.Collectors.toList());

        return new PageImpl<>(dtos, pageable, total);
    }
    public void deleteDemandeAsAdmin(String id) {
        Demande demande = getById(id);

        // Supprimer tous les bookings liés à cette demande
        List<Booking> bookings = bookingRepository.findByDemandeId(id);
        if (bookings != null && !bookings.isEmpty()) {
            bookingRepository.deleteAll(bookings);
        }

        demandeRepository.deleteById(id);
    }
    public Demande update(String id, Demande updated) {
        Demande existing = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));

        // ✅ Sauvegarder l'ANCIEN progress AVANT tout setProgress
        String oldProgress = existing.getProgress();
        System.out.println("🔍 Ancien progress: " + oldProgress);
        System.out.println("🔍 Nouveau progress: " + updated.getProgress());

        existing.setType(updated.getType());
        existing.setJourneesDestination(updated.getJourneesDestination());
        existing.setPaysResidence(updated.getPaysResidence());
        existing.setEligibiliteNote(updated.getEligibiliteNote());
        existing.setBesoinVisa(updated.isBesoinVisa());
        existing.setTypeHebergement(updated.isTypeHebergement());
        existing.setExistenceDeGarant(updated.isExistenceDeGarant());
        existing.setOuverteTouteOpportunites(updated.isOuverteTouteOpportunites());
        existing.setPreinscription(updated.isPreinscription());
        existing.setMaladieContagieuse(updated.isMaladieContagieuse());
        existing.setHandicape(updated.isHandicape());
        existing.setNiveauEtude(updated.getNiveauEtude());
        existing.setNombreAnneesEtude(updated.getNombreAnneesEtude());
        existing.setNombreMoisStage(updated.getNombreMoisStage());
        existing.setCondidatStatutActuel(updated.getCondidatStatutActuel());
        existing.setDernierPosteOccupe(updated.getDernierPosteOccupe());
        existing.setFonction(updated.getFonction());
        existing.setNombreAnneesExperience(updated.getNombreAnneesExperience());
        existing.setActivite(updated.getActivite());
        existing.setPosteSouhaite(updated.getPosteSouhaite());
        existing.setConnaissanceLinguistique(updated.getConnaissanceLinguistique());
        existing.setDiplomes(updated.getDiplomes());
        existing.setProgress(updated.getProgress());

        if (updated.getUserId() != null) {
            existing.setUserId(updated.getUserId());
        }

        // 🔥 CORRECTION ICI : Utilisation de toUpperCase() pour sécuriser la vérification
        String newProgressUpper = updated.getProgress() != null ? updated.getProgress().toUpperCase() : "";
        String oldProgressUpper = oldProgress != null ? oldProgress.toUpperCase() : "";

        if (newProgressUpper.contains("STEP_2") && !oldProgressUpper.contains("STEP_2")) {
            System.out.println("✅ Condition STEP_2 remplie — création booking...");
            autoCreateBooking(existing);
        } else {
            System.out.println("❌ Condition NON remplie — old: " + oldProgress + " new: " + updated.getProgress());
        }

        return demandeRepository.save(existing);
    }    private void autoCreateBooking(Demande demande) {
        System.out.println("🚀 autoCreateBooking appelé pour demande: " + demande.getId());

        // ℹ️ On compte ou on vérifie s'il y en a déjà, mais ON NE BLOQUE PLUS avec un "return"
        boolean alreadyExists = bookingRepository.existsByDemandeId(demande.getId());
        if (alreadyExists) {
            System.out.println("📋 Note : Cette demande possède déjà au moins un booking. Un nouveau va être ajouté.");
        }

        try {
            Booking booking = new Booking();
            booking.setUserId(demande.getUserId());
            booking.setDemandeId(demande.getId());
            booking.setUniqueId(generateNextBookingId()); // Génère proprement le BK00000X suivant
            booking.setStatus("pending");
            booking.setPaymentStatus("pending");
            booking.setInterviewStatus("En attente");
            booking.setInterviewCompleted(false);
            booking.setJourneesDestination(demande.getJourneesDestination());
            booking.setDocuments(new HashMap<>());
            booking.setDocumentsMetadata(new HashMap<>());

            // Valeurs par défaut pour les champs obligatoires
            booking.setAppointmentType("simple");
            booking.setPrice(0.0);
            booking.setAppointmentTime("09:00");
            booking.setAppointmentDate(java.time.LocalDateTime.now().plusDays(7));

            Booking saved = bookingRepository.save(booking);
            System.out.println("✅ Nouveau Booking créé avec succès ! ID: " + saved.getId() + " | UniqueID: " + saved.getUniqueId());

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la création automatique du booking: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private String generateNextBookingId() {
        Query query = new Query(Criteria.where("uniqueId").regex("^BK\\d+$"));
        query.fields().include("uniqueId");

        List<Booking> bookings = mongoTemplate.find(query, Booking.class);

        int maxId = 0;
        for (Booking b : bookings) {
            String uid = b.getUniqueId();
            if (uid != null && uid.matches("^BK\\d+$")) {
                try {
                    int num = Integer.parseInt(uid.substring(2));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }

        int nextId = maxId + 1;
        return String.format("BK%06d", nextId);
    }
}