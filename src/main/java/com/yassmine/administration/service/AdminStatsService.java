package com.yassmine.administration.service;

import com.yassmine.administration.dto.response.DashboardStatsDTO;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.Demande;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.BookingRepository;
import com.yassmine.administration.repository.DemandeRepository;
import com.yassmine.administration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final DemandeRepository demandeRepository;
    private final BookingRepository bookingRepository;

    public DashboardStatsDTO getGlobalStats(int year) {
        // Calcul des bornes de l'année de manière propre (ex: pour 2026)
        java.time.ZonedDateTime zoneStart = java.time.ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, java.time.ZoneOffset.UTC);
        java.time.Instant start = zoneStart.toInstant();
        java.time.Instant end = zoneStart.plusYears(1).minusNanos(1).toInstant();

        // ─── 1. UTILISATEURS (Filtrés directement par la BDD) ───
        List<User> usersDeLAnnee = userRepository.findAllByCreatedAtBetween(start, end);
        long totalUsers = usersDeLAnnee.size();

        Map<String, Long> usersByStatus = usersDeLAnnee.stream()
                .filter(u -> u.getCandidateVerificationStatus() != null && !u.getCandidateVerificationStatus().trim().isEmpty())
                .collect(Collectors.groupingBy(u -> u.getCandidateVerificationStatus().trim().toLowerCase(), Collectors.counting()));

        // ─── 2. DEMANDES (Filtrées directement par la BDD) ───
        List<Demande> demandesDeLAnnee = demandeRepository.findAllByCreatedAtBetween(start, end);
        long totalDemandes = demandesDeLAnnee.size();

        // Sécurisé contre les Null et la Casse (Règle le problème des mauvais comptes dans le graphe)
        Map<String, Long> demandesByStatus = demandesDeLAnnee.stream()
                .filter(d -> d.getStatus() != null)
                .collect(Collectors.groupingBy(d -> d.getStatus().trim().toLowerCase(), Collectors.counting()));

        long demandesAcceptees = demandesDeLAnnee.stream()
                .filter(d -> d.getStatus() != null && d.getStatus().trim().toLowerCase().startsWith("accepte"))
                .count();

        double eligibilityRate = totalDemandes > 0 ? ((double) demandesAcceptees / totalDemandes) * 100 : 0.0;

        // Top pays (Sécurisé)
        Map<String, Long> topCountries = demandesDeLAnnee.stream()
                .filter(d -> d.getPaysResidence() != null && d.getPaysResidence().getLabel() != null && !d.getPaysResidence().getLabel().trim().isEmpty())
                .collect(Collectors.groupingBy(d -> d.getPaysResidence().getLabel().trim(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(3)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // Top fonctions (Sécurisé)
        Map<String, Long> topFunctions = demandesDeLAnnee.stream()
                .filter(d -> d.getFonction() != null && !d.getFonction().trim().isEmpty())
                .collect(Collectors.groupingBy(d -> d.getFonction().trim(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(3)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // ─── 3. ENTRETIENS (BOOKINGS) & REVENUS ───
        List<Booking> bookingsDeLAnnee = bookingRepository.findAllByCreatedAtBetween(start, end);
        long totalBookings = bookingsDeLAnnee.size();

        double totalRevenue = bookingsDeLAnnee.stream()
                .filter(b -> b.getPaymentStatus() != null && "paid".equalsIgnoreCase(b.getPaymentStatus().trim()))
                .filter(b -> b.getPrice() != null)
                .mapToDouble(Booking::getPrice).sum();

        // Calcul du meilleur mois (Sécurisé avec conversion java.time)
        Map<java.time.Month, Long> bookingsParMois = bookingsDeLAnnee.stream()
                .collect(Collectors.groupingBy(b -> b.getCreatedAt().atZone(java.time.ZoneOffset.UTC).getMonth(), Collectors.counting()));

        Optional<Map.Entry<java.time.Month, Long>> meilleurMoisOpt = bookingsParMois.entrySet().stream().max(Map.Entry.comparingByValue());
        String bestMonthName = "Aucun";
        long bestMonthCount = 0;

        if (meilleurMoisOpt.isPresent()) {
            bestMonthName = meilleurMoisOpt.get().getKey().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);
            bestMonthName = bestMonthName.substring(0, 1).toUpperCase() + bestMonthName.substring(1);
            bestMonthCount = meilleurMoisOpt.get().getValue();
        }

        Map<String, Long> bookingsByStatus = bookingsDeLAnnee.stream()
                .filter(b -> b.getStatus() != null)
                .collect(Collectors.groupingBy(b -> b.getStatus().trim().toLowerCase(), Collectors.counting()));

        Map<String, Long> ordersByStatus = bookingsDeLAnnee.stream()
                .filter(b -> b.getPaymentStatus() != null)
                .collect(Collectors.groupingBy(b -> b.getPaymentStatus().trim().toLowerCase(), Collectors.counting()));

        Map<String, Long> interviewsByStatus = bookingsDeLAnnee.stream()
                .filter(b -> b.getInterviewStatus() != null && !b.getInterviewStatus().trim().isEmpty())
                .collect(Collectors.groupingBy(b -> b.getInterviewStatus().trim(), Collectors.counting()));

        Map<String, Long> demandesParDestination = demandesDeLAnnee.stream()
                .filter(d -> d.getJourneesDestination() != null)
                .collect(Collectors.groupingBy(Demande::getJourneesDestination, Collectors.counting()));

        Map<String, Long> entretiensParDestination = bookingsDeLAnnee.stream()
                .filter(b -> b.getJourneesDestination() != null)
                .collect(Collectors.groupingBy(Booking::getJourneesDestination, Collectors.counting()));
// On remplace les 'null' par 0.0 au lieu de les filtrer/exclure du calcul
        double avgCompletion = usersDeLAnnee.stream()
                .mapToDouble(u -> u.getProfileCompletionPercentage() != null ? u.getProfileCompletionPercentage() : 0.0)
                .average()
                .orElse(0.0);

        Map<String, Long> structureExperience = demandesDeLAnnee.stream()
                .filter(d -> d.getNombreAnneesExperience() != null)
                .collect(Collectors.groupingBy(d -> d.getNombreAnneesExperience().trim(), Collectors.counting()));

        // ─── 4. CONSTRUCTION DU DTO ───
        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalDemandes(totalDemandes)
                .totalEntretiens(totalBookings)
                .totalRevenue(totalRevenue)
                .demandesParDestination(demandesParDestination)
                .entretiensParDestination(entretiensParDestination)
                .demandesByStatus(demandesByStatus)
                .avgProfileCompletion(avgCompletion)
                .usersByStatus(usersByStatus)
                .bookingsByStatus(bookingsByStatus)
                .structureExperience(structureExperience)
                .ordersByStatus(ordersByStatus)
                .interviewsByStatus(interviewsByStatus)
                .bestMonthBookingsName(bestMonthName)
                .bestMonthBookingsCount(bestMonthCount)
                .eligibilityRate(eligibilityRate)
                .topCountries(topCountries)
                .topFunctions(topFunctions)
                .build();
    }}