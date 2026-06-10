package com.yassmine.administration.service;

import com.yassmine.administration.dto.response.DashboardStatsDTO;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.Demande;
import com.yassmine.administration.repository.DemandeRepository;
import com.yassmine.administration.repository.UserRepository;
import com.yassmine.administration.repository.BookingRepository; // Ajuste si nécessaire
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final DemandeRepository demandeRepository;
    private final BookingRepository bookingRepository;

    public DashboardStatsDTO getGlobalStats() {
        long totalUsers = userRepository.count();
        long totalDemandes = demandeRepository.count();

        List<Booking> allBookings = bookingRepository.findAll();
        long totalBookings = allBookings.size();

        // Calcul du chiffre d'affaires basé sur les rendez-vous payés
        double totalRevenue = allBookings.stream()
                .filter(b -> "paid".equalsIgnoreCase(b.getPaymentStatus()))
                .mapToDouble(Booking::getPrice)
                .sum();

        // Groupement des demandes par statut pour le graphique du dashboard
        Map<String, Long> demandesByStatus = demandeRepository.findAll().stream()
                .collect(Collectors.groupingBy(Demande::getStatus, Collectors.counting()));

        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalDemandes(totalDemandes)
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .demandesByStatus(demandesByStatus)
                .build();
    }
}