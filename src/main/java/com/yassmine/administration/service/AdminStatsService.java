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

        // ─── 1. UTILISATEURS ───
        List<User> usersDeLAnnee = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().getYear() == year)
                .collect(Collectors.toList());

        long totalUsers = usersDeLAnnee.size();

        Map<String, Long> usersByStatus = usersDeLAnnee.stream()
                .filter(u -> u.getCandidateVerificationStatus() != null && !u.getCandidateVerificationStatus().trim().isEmpty())
                .collect(Collectors.groupingBy(User::getCandidateVerificationStatus, Collectors.counting()));

        // ─── 2. DEMANDES ───
        List<Demande> demandesDeLAnnee = demandeRepository.findAll().stream()
                .filter(d -> d.getCreatedAt() != null && d.getCreatedAt().getYear() == year)
                .collect(Collectors.toList());

        long totalDemandes = demandesDeLAnnee.size();

        Map<String, Long> demandesByStatus = demandesDeLAnnee.stream()
                .collect(Collectors.groupingBy(Demande::getStatus, Collectors.counting()));

        long demandesAcceptees = demandesDeLAnnee.stream()
                .filter(d -> d.getStatus() != null && d.getStatus().toLowerCase().startsWith("accepte")).count();
        double eligibilityRate = totalDemandes > 0 ? ((double) demandesAcceptees / totalDemandes) * 100 : 0.0;

        Map<String, Long> topCountries = demandesDeLAnnee.stream()
                .filter(d -> d.getPaysResidence() != null
                        && d.getPaysResidence().getLabel() != null
                        && !d.getPaysResidence().getLabel().trim().isEmpty())
                .collect(Collectors.groupingBy(d -> d.getPaysResidence().getLabel(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(3)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        Map<String, Long> topFunctions = demandesDeLAnnee.stream()
                .filter(d -> d.getFonction() != null && !d.getFonction().trim().isEmpty())
                .collect(Collectors.groupingBy(Demande::getFonction, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(3)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // ─── 3. ENTRETIENS (BOOKINGS) & REVENUS ───
        List<Booking> bookingsDeLAnnee = bookingRepository.findAll().stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().getYear() == year)
                .collect(Collectors.toList());

        long totalBookings = bookingsDeLAnnee.size();

        double totalRevenue = bookingsDeLAnnee.stream()
                .filter(b -> "paid".equalsIgnoreCase(b.getPaymentStatus()))
                .mapToDouble(Booking::getPrice).sum();

        Map<Month, Long> bookingsParMois = bookingsDeLAnnee.stream()
                .collect(Collectors.groupingBy(b -> b.getCreatedAt().getMonth(), Collectors.counting()));

        Optional<Map.Entry<Month, Long>> meilleurMoisOpt = bookingsParMois.entrySet().stream().max(Map.Entry.comparingByValue());
        String bestMonthName = "Aucun";
        long bestMonthCount = 0;

        if (meilleurMoisOpt.isPresent()) {
            bestMonthName = meilleurMoisOpt.get().getKey().getDisplayName(TextStyle.FULL, Locale.FRENCH);
            bestMonthName = bestMonthName.substring(0, 1).toUpperCase() + bestMonthName.substring(1);
            bestMonthCount = meilleurMoisOpt.get().getValue();
        }

        Map<String, Long> bookingsByStatus = bookingsDeLAnnee.stream()
                .filter(b -> b.getStatus() != null)
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));

        Map<String, Long> ordersByStatus = bookingsDeLAnnee.stream()
                .filter(b -> b.getPaymentStatus() != null)
                .collect(Collectors.groupingBy(Booking::getPaymentStatus, Collectors.counting()));


        // ─── 4. CONSTRUCTION DU DTO ───
        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalDemandes(totalDemandes)
                .totalEntretiens(totalBookings)
                .totalRevenue(totalRevenue)

                .demandesByStatus(demandesByStatus)
                .usersByStatus(usersByStatus)         // ← J'ai ajouté l'injection manquante ici
                .bookingsByStatus(bookingsByStatus)
                .ordersByStatus(ordersByStatus)

                .bestMonthBookingsName(bestMonthName)
                .bestMonthBookingsCount(bestMonthCount)
                .eligibilityRate(eligibilityRate)
                .topCountries(topCountries)
                .topFunctions(topFunctions)
                .build(); // ← J'ai supprimé le doublon .totalUsers(totalUsers)
    }
}