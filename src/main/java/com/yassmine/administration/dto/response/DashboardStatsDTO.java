package com.yassmine.administration.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalDemandes;
    private long totalEntretiens; ;
    private double totalRevenue;
    private Map<String, Long> demandesByStatus; // Ex: {"pre_selection": 5, "accepte_pour_entretien": 1}
    private String bestMonthBookingsName;
    private long bestMonthBookingsCount;
    private double eligibilityRate;
    private Map<String, Long> topCountries;
    private Map<String, Long> topFunctions;
    private Map<String, Long> usersByStatus;     // ex: ADMIN, CANDIDAT
    private Map<String, Long> bookingsByStatus; // ex: CONFIRMED, PENDING, CANCELLED
    private Map<String, Long> ordersByStatus;   // ex: PAID, UNPAID (basé sur le PaymentStatus)

}